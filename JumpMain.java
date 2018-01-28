package JumpForJump;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.imageio.ImageIO;

public class JumpMain {

	
	private static String ADBCaptureCommand = "adb shell screencap -p /sdcard/haha.png";
	private static String ADBPullCommand = "adb pull /sdcard/haha.png D:\\haha.png";
	private static String URL_IMG = "D:\\haha.png";
	private static BufferedImage image =null;
	private static int count=0;
	
	//主要用于距离于像素点之间不成正比关系，可随机调整，1980*1080 修改这几个参数
	private static double lBounce=2.0561;
	private static double xsBounce=2.0761;
	private static double xssBounce=1.9761;
	
	private static int[] currentXY=new int[2];
	private static int[] nextTopXY=new int[2];
	private static int[] nextBottomXY=new int[2];
	private static int[] nextGridXY=new int[2];
	
	//取得像素点位置，主要用于确定下个顶点位置
	private static int[] pixelXY=new int[2];
	
	public static void main(String[] args) throws Exception {
		
		JumpMain JumpMainTest=new JumpMain();
		JumpMainTest.executeADBCommand("adb shell screencap -p /sdcard/haha.png");
		JumpMainTest.executeADBCommand("adb pull /sdcard/haha.png "+"D:\\image\\lenght.png");
		image = ImageIO.read(new File("D:\\image\\lenght.png"));
		
		pixelXY[0]=(int) Math.round(image.getWidth()*0.1888);
		pixelXY[1]=(int) Math.round(image.getHeight()*0.16382);
		for(int i=0;i<10000;i++){
			try {
				
				Date date=new Date();
				SimpleDateFormat sdf=new SimpleDateFormat("HH-mm-ss");
				String fileName ="D:\\image\\"+ sdf.format(date)+".png";
				String command="";
				//执行adb命令
				command="adb shell screencap -p /sdcard/haha.png";
				JumpMainTest.executeADBCommand(command);
				Thread.sleep(1100);
				command="adb pull /sdcard/haha.png "+fileName;
				JumpMainTest.executeADBCommand(command);
				System.out.println(fileName);
				image = ImageIO.read(new File(fileName));
				currentXY=JumpMainTest.calcCurrentLocal();
				//判断是否需要重新开局
				if(currentXY[0]==0&&currentXY[1]==0){
					command="adb shell input swipe 350 1060 350 1060 100";
					//是否继续开始？
					//JumpMainTest.executeADBCommand(command);
					File ins = new File(fileName);
					File outs =new File("D:\\score\\"+sdf.format(date)+".png");
					//保存成绩？
					//JumpMainTest.copyFile(ins, outs);
					ImageIO.write(image, "png", outs);
					Thread.sleep(5000);
					break;
				}
				System.out.println("currentX:"+currentXY[0]+"\tcurrentY:"+currentXY[1]);
				nextTopXY=JumpMainTest.calcNextTopXY(image);
				
				System.out.println("旗子高度减去定点高度："+Math.abs(currentXY[1]-nextTopXY[1]));
				
				if(Math.abs(currentXY[1]-nextTopXY[1])<=130){
					nextTopXY=JumpMainTest.calcNextTopXY(image, currentXY);
					System.out.println("旗子高度高于下个格子");
				}
				
				System.out.println("nextTopX:"+nextTopXY[0]+"\tnextTopY:"+nextTopXY[1]);
				nextBottomXY=JumpMainTest.calcNextBottomXY(image, nextTopXY);
				System.out.println("nextBottomX:"+nextBottomXY[0]+"\tnextBottomY:"+nextBottomXY[1]);
				nextGridXY=JumpMainTest.calcNextGridLocal(nextTopXY, nextBottomXY);
				System.out.println("nextGridX:"+nextGridXY[0]+"\tnextGridY:"+nextGridXY[1]);
				double calcDistance = JumpMainTest.calcDistance(currentXY, nextGridXY);
				int distance=(int)Math.round((calcDistance*lBounce));
				
				if(distance<=700){
					distance=(int)Math.round((calcDistance*xsBounce));
				}
				command=String.format("adb shell input swipe 500 500 500 500 %s",  distance);
				JumpMainTest.executeADBCommand(command);
				System.out.println(command);
				System.out.println("distance:"+distance);
				
				//休息一下
				System.out.println("循环："+(i+1));
				int time=new Random().nextInt(5510);
				System.out.println("休息："+time);
				Thread.sleep(time);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
		}
		
	}
	
	/**
	 * 执行跳一跳命令
	 */
	public void executeADBCommand(String command){
		Process process = null;
		BufferedWriter bw = null;

		BufferedReader errorReader = null;
		try {
			process = Runtime.getRuntime().exec(command);
			bw = new BufferedWriter(new OutputStreamWriter(
					process.getOutputStream()));
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					process.getInputStream(), "GBK"));
			errorReader = new BufferedReader(new InputStreamReader(
					process.getErrorStream()));
			 process.waitFor();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(process!=null){
			process.destroy();
		}
		
	}

	
	

	/**用于计算当前人物在哪个坐标
	 * @return
	 */
	public int[] calcCurrentLocal() {
		int[] broad=new int[2];
		try {
			boolean flag=false;
			int xBroad=24;
			int yBroad=5;
			
			//此循环用于确定人物位置
			for (int i = 0; i < image.getWidth(); i++) {
				for (int j = 0; j < image.getHeight(); j++) {
					int pixel = image.getRGB(i,j );
					// System.out.println("x:"+((pixel&0xff0000)>>16)+"y:"+((pixel&0xff00)>>8)+"z:"+(pixel&0xff));
					int x=((pixel&0xff0000)>>16);
					int y=((pixel&0xff00)>>8);
					int z=(pixel&0xff);
					int xAver=Math.abs(x-43);
					int yAver=Math.abs(y-43);
					int zAver=Math.abs(z-73);
					//用于确定旗子位置
					if((xAver+yAver+zAver)<8){
						broad[0]=xBroad+i;
						broad[1]=yBroad+j;
						flag=true;
						break;
					}	
				}	
				if(flag){
					break;
				}	
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return broad;
	}
	
	
	/**
	 * 返回下一个格子顶点坐标
	 * @param image
	 * @return
	 */
	public int[] calcNextTopXY(BufferedImage image){
		boolean flag=false;
		int temp = image.getRGB(pixelXY[0], pixelXY[1]);
		int xFlat=((temp&0xff0000)>>16);
		int yFlat=((temp&0xff00)>>8);
		int zFlat=((temp&0xff));
		int[] topXY=new int[2];
		
		for (int j = pixelXY[1]; j < image.getHeight(); j++) {
			int staticX=0;
			for (int i = pixelXY[0]; i < image.getWidth()-1; i++) {
				int pixel = image.getRGB(i, j);
				int x=((pixel&0xff0000)>>16);
				int y=((pixel&0xff00)>>8);
				int z=(pixel&0xff);
				
				//当旗子高度高于下一个格子的情况
				if(((Math.abs(x-xFlat)+Math.abs(y-yFlat)+Math.abs(z-zFlat))/3>10)){
					int nextPixel = image.getRGB(i+1, j);
					int nextX=((nextPixel&0xff0000)>>16);
					int nextY=((nextPixel&0xff00)>>8);
					int nextZ=(nextPixel&0xff);
					if(((Math.abs(x-nextX)+Math.abs(y-nextY)+Math.abs(z-nextZ))/3<10)){
						staticX++;
					}
					else {
						
						topXY[0]=(i-(staticX/2));
						topXY[1]=j;
						flag=true;
						return topXY;
					}
				}	
			}
			if(flag){
				break;
			}
		}
		
		return topXY;
	}
	
	/**
	 * 返回下一个格子下边图标
	 * @param image
	 * @return
	 */
	public int[] calcNextBottomXY(BufferedImage image,int[] nextTopXY){
		boolean flag=false;
		int temp = image.getRGB(nextTopXY[0], nextTopXY[1]);
		int xFlat=((temp&0xff0000)>>16);
		int yFlat=((temp&0xff00)>>8);
		int zFlat=((temp&0xff));
		int[] bottomXY=new int[2];
		for(int i=currentXY[1]-1;i>nextTopXY[1]-2;i--){
			int pixel = image.getRGB(nextTopXY[0], i);
			int x=((pixel&0xff0000)>>16);
			int y=((pixel&0xff00)>>8);
			int z=(pixel&0xff);
			if(((Math.abs(x-xFlat)+Math.abs(y-yFlat)+Math.abs(z-zFlat))/3<8)){
				bottomXY[0]=nextTopXY[0];
				bottomXY[1]=i;
				break;
			}
		}
		return bottomXY;
	}
	
	/**
	 * 返回下个格子坐标
	 * @param i
	 * @param j
	 * @return
	 */
	public int[] calcNextGridLocal(int[] i,int[] j){
		int[] temp=new int[2];
		temp[0]=j[0];
		temp[1]=(i[1]+j[1])/2;
		return temp;
	}

	/**
	 * 计算距离
	 * @param i
	 * @param j
	 * @return
	 */
	public double calcDistance(int[] i,int[] j){	
		
		return Math.sqrt(Math.pow(Math.abs(i[0]-j[0]), 2)+Math.pow(Math.abs(i[1]-j[1]), 2));	
	}
	
	
	/**
	 * @param ins
	 * @param outs
	 * @throws IOException
	 * 2018年1月26日 下午5:26:15
	 */
	public void copyFile(File ins,File outs) throws IOException {
		FileInputStream fis = new FileInputStream(ins);
		FileOutputStream fos = new FileOutputStream(outs);

		byte[] b = new byte[1024];
		int n = 0;
		while ((n = fis.read(b)) != -1) {
			fos.write(b, 0, n);
		}
		
		if(fis!=null){
			fis.close();
		}
		if(fos!=null){
			fos.close();
		}
	}
	
	/**
	 * 重载函数，用于计算人物高度高于下个格子情况
	 * @param image
	 * @param currentXY
	 * @return
	 */
	public int[] calcNextTopXY(BufferedImage image,int[] currentXY){
		boolean flag=false;
		int temp = image.getRGB(currentXY[0]-5, currentXY[1]-170);
		int xFlat=((temp&0xff0000)>>16);
		int yFlat=((temp&0xff00)>>8);
		int zFlat=((temp&0xff));
		int[] topXY=new int[2];
		
		for (int j = currentXY[1]-131; j < currentXY[1]; j++) {
			int staticX=0;
			for (int i = 0;(i < image.getWidth()); i++) {
				int pixel = image.getRGB(i, j);
				int x=((pixel&0xff0000)>>16);
				int y=((pixel&0xff00)>>8);
				int z=(pixel&0xff);
				//当旗子高度高于下一个格子的情况
				if(!((i > currentXY[0]-30)&&(i < currentXY[0]+30))){
					if(((Math.abs(x-xFlat)+Math.abs(y-yFlat)+Math.abs(z-zFlat))/3>10)){
						int nextPixel = image.getRGB(i+1, j);
						int nextX=((nextPixel&0xff0000)>>16);
						int nextY=((nextPixel&0xff00)>>8);
						int nextZ=(nextPixel&0xff);
						if(((Math.abs(x-nextX)+Math.abs(y-nextY)+Math.abs(z-nextZ))/3<4)){
							staticX++;
						}
						else {
							topXY[0]=(i-staticX/2);
							topXY[1]=j;
							flag=true;
							return topXY;
						}
					}
				}
				
				
			}
			if(flag){
				break;
			}
		}
		
		return topXY;
	}
}
