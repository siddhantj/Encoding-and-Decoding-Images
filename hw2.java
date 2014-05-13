import java.awt.Graphics;
import java.awt.image.BufferedImage;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


class DrawPanel extends JPanel
{
	public void paintComponent(Graphics g)
	{
		g.drawImage(hw2.img2,0,0,null);
		g.dispose();
	}
}

class DoOperations
{
	int i,j;	//image traverse
	int x,y;	// 8x8 block treverse
	int x1,y1;
	int u,v;	// for DCT values
	int v1,u1;  // every block must have summation from (0,0) to (7,7)
	int u2,v2;  // IDCT same reason above
	
	int red,green,blue;
	int pix;
	
	private double quantScale;
	private double latency;
	private double[][] cosTable;
	
	private double[][] fullRed;
	private double[][] fullGreen;
	private double[][] fullBlue;
	
	
	private double[][] fullRedDCT;
	private double[][] fullGreenDCT;
	private double[][] fullBlueDCT;
	
	private double[][] quantizedRed;
	private double[][] quantizedGreen;
	private double[][] quantizedBlue;
	
	
	private double[][] dequantizedRed;
	private double[][] dequantizedGreen;
	private double[][] dequantizedBlue;
	
	/*private String[][] binaryDequantizedRed;
	private String[][] binaryDequantizedGreen;
	private String[][] binaryDequantizedBlue; */
	JPanel draw;
	JFrame frame;
    double cu,cv;
	
	public DoOperations()
	{
		cosTable= new double[512][512]; 
		for(int a=0;a<512;a++)
			 for(int b=0;b<512;b++)
			 {
				 cosTable[a][b]=Math.cos(((2*a +1)*b*Math.PI)/16);
			 }
		 
		 fullRed=new double[hw2.height][hw2.width];
		 fullGreen=new double[hw2.height][hw2.width];
		 fullBlue=new double[hw2.height][hw2.width];
		 
		 fullRedDCT=new double[hw2.height][hw2.width];
		 fullGreenDCT=new double[hw2.height][hw2.width];
		 fullBlueDCT=new double[hw2.height][hw2.width];
		
		 quantizedRed=new double[hw2.height][hw2.width];
		 quantizedGreen=new double[hw2.height][hw2.width];
		 quantizedBlue=new double[hw2.height][hw2.width];
		 
		 dequantizedRed=new double[hw2.height][hw2.width];
		 dequantizedGreen=new double[hw2.height][hw2.width];
		 dequantizedBlue=new double[hw2.height][hw2.width];
		 
		 /*binaryDequantizedRed= new String[hw2.height][hw2.width];
		  binaryDequantizedGreen= new String[hw2.height][hw2.width];
		  binaryDequantizedBlue= new String[hw2.height][hw2.width];*/
		 
		quantScale=hw2.quantizationLevel;	//parameter has to be supplied via command line
		latency=hw2.latency;
		
		frame=new JFrame();
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setSize(512, 512);
	    frame.setTitle("Computed Image");
	    draw = new DrawPanel();
	    frame.getContentPane().add(draw);
	    frame.setVisible(true); 
		 
	}
	
	public void dCT()
	{
		
		/* Covert to red,green,blue */
		for(j=0;j<hw2.height;j++)
		{
			for(i=0;i<hw2.width;i++)
			{
				pix=hw2.Image1[j][i];
				red=(pix >>>16) & 0xff;
				green=(pix >>>8) & 0xff;
				blue=(pix) & 0xff;
				fullRed[j][i]=red-128;
				fullGreen[j][i]=green-128;
				fullBlue[j][i]=blue-128;
								
			}
		}
		
		/* Apply DCT here */
		for(y=0;y<hw2.height;y+=8)
		{
			for(x=0;x<hw2.width;x+=8)
			{
				u1=0;
				v1=0;
				for(v=y;v<y+8;v++)
				{
					for(u=x;u<x+8;u++)
					{
						if(u==x)
							cu=1/Math.sqrt(2);
						else
							cu=1;
						if(v==y)
							cv=1/Math.sqrt(2);
						else
							cv=1;
						
						for(y1=y;y1<y+8;y1++)
						{
							for(x1=x;x1<x+8;x1++)
							{
								
								fullRedDCT[v][u]=fullRedDCT[v][u] + fullRed[y1][x1]* Math.cos(((2*x1+1)*u1*Math.PI)/16) * Math.cos(((2*y1+1)*v1*Math.PI)/16);
								fullGreenDCT[v][u]=fullGreenDCT[v][u] + fullGreen[y1][x1]* Math.cos(((2*x1+1)*u1*Math.PI)/16) * Math.cos(((2*y1+1)*v1*Math.PI)/16);
								fullBlueDCT[v][u]=fullBlueDCT[v][u] + fullBlue[y1][x1]* Math.cos(((2*x1+1)*u1*Math.PI)/16) * Math.cos(((2*y1+1)*v1*Math.PI)/16);
								
							}
						}
						fullRedDCT[v][u]=fullRedDCT[v][u]*((double)1/4*cu*cv);
						fullGreenDCT[v][u]=fullGreenDCT[v][u]*((double)1/4*cu*cv);
						fullBlueDCT[v][u]=fullBlueDCT[v][u]*((double)1/4*cu*cv);
						u1++;	
					}
					u1=0;
					v1++;
				}
			}
		}
		
	}
		
		public void quantize() 
		{
			double tempR,tempG,tempB;
			for(j=0;j<hw2.height;j++)
			{
				for(i=0;i<hw2.width;i++)
				{
					tempR=fullRedDCT[j][i]/Math.pow(2.0, quantScale);					
					quantizedRed[j][i]=(int)tempR;
					
					tempG=fullGreenDCT[j][i]/Math.pow(2.0, quantScale);					
					quantizedGreen[j][i]=(int)tempG;
					
					tempB=fullBlueDCT[j][i]/Math.pow(2.0, quantScale);					
					quantizedBlue[j][i]=(int)tempB;
					
					
				}
			}
		}
		
		public void dequantize()
		{
			double tempR,tempG,tempB;
			int length;
			for(j=0;j<hw2.height;j++)
			{
				for(i=0;i<hw2.width;i++)
				{
					tempR=quantizedRed[j][i]*Math.pow(2.0, quantScale);
					dequantizedRed[j][i]=tempR;
						/*if(tempR-(int)tempR>0.5)
						dequantizedRed[j][i]=Math.ceil(tempR);
					else
						dequantizedRed[j][i]=Math.floor(tempR); */
					/*binaryDequantizedRed[j][i]=Integer.toBinaryString((int)tempR);
					while(binaryDequantizedRed[j][i].length()!=32)
					{
						binaryDequantizedRed[j][i]="0"+binaryDequantizedRed[j][i];						
					}*/
					
					
					tempG=quantizedGreen[j][i]*Math.pow(2.0, quantScale);
					dequantizedGreen[j][i]=tempG;
					/*if(tempG-(int)tempG>0.5)
						dequantizedGreen[j][i]=Math.ceil(tempG);
					else
						dequantizedGreen[j][i]=Math.floor(tempG);*/ 
					/*binaryDequantizedGreen[j][i]=Integer.toBinaryString((int)tempG);
					while(binaryDequantizedGreen[j][i].length()!=32)
					{
						binaryDequantizedGreen[j][i]="0"+binaryDequantizedGreen[j][i];						
					}*/
					
					tempB=quantizedBlue[j][i]*Math.pow(2.0,quantScale);
					dequantizedBlue[j][i]=tempB;
					/*if(tempB-(int)tempB>0.5)
						dequantizedBlue[j][i]=Math.ceil(tempB);
					else
						dequantizedBlue[j][i]=Math.floor(tempB); */
					/*binaryDequantizedBlue[j][i]=Integer.toBinaryString((int)tempB);
					while(binaryDequantizedBlue[j][i].length()!=32)
					{
						binaryDequantizedBlue[j][i]="0"+binaryDequantizedBlue[j][i];						
					}*/
					
				}
			}
		}
		
		public void iDCT_baseline() throws InterruptedException
		{
			
		/* Apply IDCT here */
		fullRed=new double[hw2.height][hw2.width]; /* These values were previously filled and need to be cleared to work with them*/
		fullGreen=new double[hw2.height][hw2.width];
		fullBlue=new double[hw2.height][hw2.width];
		
		for(v=0;v<hw2.height;v=v+8)
		{
			for(u=0;u<hw2.width;u=u+8)
			{
				
				for(y=v;y<v+8;y++)
				{
					for(x=u;x<u+8;x++)
					{
						v2=0;
						u2=0;
						for(v1=v;v1<v+8;v1++)
						{
							for(u1=u;u1<u+8;u1++)
							{
								if(u==u1)
									cu=1/Math.sqrt(2);
								else
									cu=1;
								if(v==v1)
									cv=1/Math.sqrt(2);
								else
									cv=1;
								fullRed[y][x]=fullRed[y][x] + cu*cv*dequantizedRed[v1][u1]*(cosTable[x][u2])*(cosTable[y][v2]);
								fullGreen[y][x]=fullGreen[y][x] + cu*cv*dequantizedGreen[v1][u1]*(cosTable[x][u2])*(cosTable[y][v2]);
								fullBlue[y][x]=fullBlue[y][x] + cu*cv*dequantizedBlue[v1][u1]*(cosTable[x][u2])*(cosTable[y][v2]);
								u2++;
							}
							u2=0;
							v2++;
						}
							fullRed[y][x]=fullRed[y][x]/4;
							fullGreen[y][x]=fullGreen[y][x]/4;
							fullBlue[y][x]=fullBlue[y][x]/4;
					}
					
				}
				/*block type display */
				Thread.sleep((long)latency);
				reconstructImage();	
			}
		}
		
	}
		
		public void iDCT_spectral() throws InterruptedException
		{
			double[][] spectralRed,spectralGreen,spectralBlue;
			int p,q,i,j;
			/* Apply IDCT here */
				
				spectralRed=new double[hw2.height][hw2.width];
				spectralGreen=new double[hw2.height][hw2.width];
				spectralBlue=new double[hw2.height][hw2.width];
				p=0;q=0;
				
				while(p<8 && q<8)
				{
					fullRed=new double[hw2.height][hw2.width]; /* These values were previously filled and need to be cleared to work with them*/
					fullGreen=new double[hw2.height][hw2.width];
					fullBlue=new double[hw2.height][hw2.width];
					for(j=q;j<hw2.height;j=j+8)
					{
						for(i=p;i<hw2.width;i=i+8)
						{
							spectralRed[j][i]=dequantizedRed[j][i];
							spectralGreen[j][i]=dequantizedGreen[j][i];
							spectralBlue[j][i]=dequantizedBlue[j][i];
						}
					}
					p++;
					if(p>=8)
					{
						p=0;
						q++;
					}
									
							
			for(v=0;v<hw2.height;v=v+8)
			{
				for(u=0;u<hw2.width;u=u+8)
				{
					
					for(y=v;y<v+8;y++)
					{
						for(x=u;x<u+8;x++)
						{
							v2=0;
							u2=0;
							for(v1=v;v1<v+8;v1++)
							{
								for(u1=u;u1<u+8;u1++)
								{
									if(u==u1)
										cu=1/Math.sqrt(2);
									else
										cu=1;
									if(v==v1)
										cv=1/Math.sqrt(2);
									else
										cv=1;
									fullRed[y][x]=fullRed[y][x] + cu*cv*spectralRed[v1][u1]*(cosTable[x][u2])*(cosTable[y][v2]);
									fullGreen[y][x]=fullGreen[y][x] + cu*cv*spectralGreen[v1][u1]*(cosTable[x][u2])*(cosTable[y][v2]);
									fullBlue[y][x]=fullBlue[y][x] + cu*cv*spectralBlue[v1][u1]*(cosTable[x][u2])*(cosTable[y][v2]);
									u2++;
								}
								u2=0;
								v2++;
							}
								fullRed[y][x]=fullRed[y][x]/4;
								fullGreen[y][x]=fullGreen[y][x]/4;
								fullBlue[y][x]=fullBlue[y][x]/4;
								
						}
						
					}
					//System.out.println("fullRed[0][0]= "+fullRed[0][0]);
					
				}
			}
			Thread.sleep((long)latency);
			reconstructImage();
				}
			
		}
		
		public void iDCT_bitApprox() throws InterruptedException
		{
			double[][] bitApproxRed,bitApproxGreen,bitApproxBlue;
			int p,q;
			int index=0;
			int red_bit,green_bit,blue_bit;
			double temp_red,temp_green,temp_blue;
			String binary_red=null,binary_green=null,binary_blue=null;
			bitApproxRed=new double[hw2.height][hw2.width];
			bitApproxGreen=new double[hw2.height][hw2.width];
			bitApproxBlue= new double[hw2.height][hw2.width];
			double stringLength=16;
			while(index<16)		//while(stringLength>=0 && index<32)
			{
				//System.out.println("Index: "+index+"  StrLength: "+(stringLength-1));
				fullRed=new double[hw2.height][hw2.width]; /* These values were previously filled and need to be cleared to work with them*/
				fullGreen=new double[hw2.height][hw2.width];
				fullBlue=new double[hw2.height][hw2.width];
				for(q=0;q<hw2.height;q++)
				{
					for(p=0;p<hw2.width;p++)
					{
						if(dequantizedRed[q][p]<0)
						{
							temp_red=dequantizedRed[q][p] * -1;
							binary_red=Integer.toBinaryString((int)temp_red);
							while(binary_red.length()!=16)
							{
								binary_red="0"+binary_red;
							}
							red_bit=binary_red.charAt(index)-48;
							temp_red= red_bit*Math.pow(2.0, stringLength-1);	//4		1
							temp_red=temp_red*-1;	//-4	-1
							bitApproxRed[q][p]=bitApproxRed[q][p]+temp_red;		// -4 + -1=-5
						}
						else
						{
							temp_red=dequantizedRed[q][p];
							binary_red=Integer.toBinaryString((int)temp_red);
							while(binary_red.length()!=16)
							{
								binary_red="0"+binary_red;
							}
							red_bit=binary_red.charAt(index)-48;
							temp_red= red_bit*Math.pow(2.0, stringLength-1);
							bitApproxRed[q][p]=bitApproxRed[q][p]+temp_red;
						}
						if(dequantizedGreen[q][p]<0)
						{
							temp_green=dequantizedGreen[q][p] * -1;
							binary_green=Integer.toBinaryString((int)temp_green);
							while(binary_green.length()!=16)
							{
								binary_green="0"+binary_green;
							}
							green_bit=binary_green.charAt(index)-48;
							temp_green= green_bit*Math.pow(2.0, stringLength-1);	//4		1
							temp_green=temp_green*-1;	//-4	-1
							bitApproxGreen[q][p]=bitApproxGreen[q][p]+temp_green;		// -4 + -1=-5
						}
						else
						{
							temp_green=dequantizedGreen[q][p];
							binary_green=Integer.toBinaryString((int)temp_green);
							while(binary_green.length()!=16)
							{
								binary_green="0"+binary_green;
							}
							green_bit=binary_green.charAt(index)-48;
							temp_green= green_bit*Math.pow(2.0, stringLength-1);
							bitApproxGreen[q][p]=bitApproxGreen[q][p]+temp_green;
						}
						if(dequantizedBlue[q][p]<0)
						{
							temp_blue=dequantizedBlue[q][p] * -1;
							binary_blue=Integer.toBinaryString((int)temp_blue);
							while(binary_blue.length()!=16)
							{
								binary_blue="0"+binary_blue;
							}
							blue_bit=binary_blue.charAt(index)-48;
							temp_blue= blue_bit*Math.pow(2.0, stringLength-1);	//4		1
							temp_blue=temp_blue*-1;	//-4	-1
							bitApproxBlue[q][p]=bitApproxBlue[q][p]+temp_blue;		// -4 + -1=-5
						}
						else
						{
							temp_blue=dequantizedBlue[q][p];
							binary_blue=Integer.toBinaryString((int)temp_blue);
							while(binary_blue.length()!=16)
							{
								binary_blue="0"+binary_blue;
							}
							blue_bit=binary_blue.charAt(index)-48;
							temp_blue= blue_bit*Math.pow(2.0, stringLength-1);
							bitApproxBlue[q][p]=bitApproxBlue[q][p]+temp_blue;
						}
						
					}
				}
				index++;
				stringLength--;
				for(v=0;v<hw2.height;v=v+8)
				{
					for(u=0;u<hw2.width;u=u+8)
					{
						
						for(y=v;y<v+8;y++)
						{
							for(x=u;x<u+8;x++)
							{
								v2=0;
								u2=0;
								for(v1=v;v1<v+8;v1++)
								{
									for(u1=u;u1<u+8;u1++)
									{
										if(u==u1)
											cu=1/Math.sqrt(2);
										else
											cu=1;
										if(v==v1)
											cv=1/Math.sqrt(2);
										else
											cv=1;
										fullRed[y][x]=fullRed[y][x] + cu*cv*bitApproxRed[v1][u1]*(cosTable[x][u2])*(cosTable[y][v2]);
										fullGreen[y][x]=fullGreen[y][x] + cu*cv*bitApproxGreen[v1][u1]*(cosTable[x][u2])*(cosTable[y][v2]);
										fullBlue[y][x]=fullBlue[y][x] + cu*cv*bitApproxBlue[v1][u1]*(cosTable[x][u2])*(cosTable[y][v2]);
										u2++;
									}
									u2=0;
									v2++;
								}
									fullRed[y][x]=fullRed[y][x]/4;
									fullGreen[y][x]=fullGreen[y][x]/4;
									fullBlue[y][x]=fullBlue[y][x]/4;
									
							}
							
						}
						
						
					}
				}
				//System.out.println("fullRed[0][0]= "+fullRed[0][0]+" bitApproxRed[0][0] "+bitApproxRed[0][0]);
				
				Thread.sleep((long)latency);
				reconstructImage();	
			}
		}	
		
		
		public void reconstructImage()
		{
			int int_red,int_green,int_blue;
			int pix1;
			hw2.img2 = new BufferedImage(hw2.width, hw2.height, BufferedImage.TYPE_INT_RGB);
			for(j=0;j<hw2.height;j++)
			{
				for(i=0;i<hw2.width;i++)
				{
					int_red=(int)fullRed[j][i]+128;
					int_green=(int)fullGreen[j][i]+128;
					int_blue=(int)fullBlue[j][i]+128;
					if(int_red>255)
						int_red=255;
					if(int_red<0)
						int_red=0;
					
					if(int_green>255)
						int_green=255;
					if(int_green<0)
						int_green=0;
					
					if(int_blue>255)
						int_blue=255;
					if(int_blue<0)
						int_blue=0;
					
					pix1 = 0xff000000 | ((int_red & 0xff) << 16) | ((int_green & 0xff) << 8) | (int_blue & 0xff);
					hw2.img2.setRGB(i, j, pix1);
					
				}
			}
			draw.repaint();
			
		}
		
}

public class hw2 {
public static BufferedImage img;
public static BufferedImage img2;
public static int[][] Image1;
public static int[][] Image2;
public static double quantizationLevel;
public static int deliveryMode;
public static double latency;

public static int width,height;
public static byte[] red,green,blue;

public static void main(String[] args) throws InterruptedException
	{
	String fileName = args[0];
	width=352;											
	height=288;		
	quantizationLevel=Double.parseDouble(args[1]);
	try
	{
		deliveryMode=Integer.parseInt(args[2]);
	}catch(Exception e)
	{
		System.out.println("Invalid input");
		System.exit(1);
	}
	latency=Double.parseDouble(args[3]);
	if(quantizationLevel<0 || quantizationLevel>7)
	{
		System.out.println("Invalid input");
		System.exit(1);
	}
	if(deliveryMode<1 || deliveryMode>3)
	{
		System.out.println("Invalid input");
		System.exit(1);
	}
	
	
	DoOperations d=new DoOperations();
	
		//double rotationRequired=Double.parseDouble(args[4]);
	Image1 = new int[height][width];	//original image
	
	
    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    try {
	    File file = new File(args[0]);
	    InputStream is = new FileInputStream(file);

	    long len = file.length();	// image size*1024
	    byte[] bytes = new byte[(int)len];
	    
	    int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
    		
    	int ind = 0;
		for(int y = 0; y < height; y++){
	
			for(int x = 0; x < width; x++){
		 
				byte a = 0;
				byte r = bytes[ind];
				byte g = bytes[ind+height*width];
				byte b = bytes[ind+height*width*2]; 
				
				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				Image1[y][x]=pix;	// storing pixels of orignal image
				//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				img.setRGB(x,y,pix);
				ind++;
			}
		}
		
		
    } catch (FileNotFoundException e) {
      System.out.println("File not found");
      System.exit(1);
    } catch (IOException e) {
      System.out.println("File not found");
      System.exit(1);
    } 
    d.dCT();
    d.quantize();
    d.dequantize();
    if(deliveryMode==1)
    	d.iDCT_baseline();
    if(deliveryMode==2)
    	d.iDCT_spectral();
    if(deliveryMode==3)
    	d.iDCT_bitApprox();
   
    
    JFrame frame=new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1024, 768);
    frame.setTitle("Original Image");
    JLabel label = new JLabel(new ImageIcon(hw2.img));
    frame.getContentPane().add(label);
    frame.setVisible(true); 
    frame.pack();
    
    
   
	}

}
