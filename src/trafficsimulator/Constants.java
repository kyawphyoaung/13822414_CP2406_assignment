
package trafficsimulator;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Constants {
    public static final int VT_BUS=0;
    public static final int VT_CAR=1;
    public static final int VT_MORTORBIKE=2;
    public static String[] STR_VEHICLE_NAMES=new String[]{"Bus","Car","Motorbike"};
    
    public static double[] BREADTH=new double[STR_VEHICLE_NAMES.length];
    public static double[] LENGTH=new double[STR_VEHICLE_NAMES.length];

    public static double ROAD_BREADTH=120.0;
    
    public static BufferedImage[][] BI_VEHICLE=new BufferedImage[STR_VEHICLE_NAMES.length][360];
    static{
        double car_breadth=20; // even number
        BREADTH[VT_CAR]=car_breadth;
        LENGTH[VT_CAR]=BREADTH[VT_CAR]*2;
        BREADTH[VT_BUS]=BREADTH[VT_CAR]*1.5;
        LENGTH[VT_BUS]=LENGTH[VT_CAR]*1.5;
        BREADTH[VT_MORTORBIKE]=BREADTH[VT_CAR]/2;
        LENGTH[VT_MORTORBIKE]=BREADTH[VT_MORTORBIKE]*2;
        
        for(int i=0;i<STR_VEHICLE_NAMES.length;i++){
            try {
                File file=new File("images/"+STR_VEHICLE_NAMES[i]+".png");
                BI_VEHICLE[i][0]=ImageIO.read(file);
                for(int degree=1;degree<360;degree++){
                    double sin = Math.abs(Math.sin(Math.toRadians(degree)));
                    double cos = Math.abs(Math.cos(Math.toRadians(degree)));                    
                    int w = (int) Math.floor(BI_VEHICLE[i][0].getWidth() * cos + BI_VEHICLE[i][0].getHeight() * sin);
                    int h = (int) Math.floor(BI_VEHICLE[i][0].getHeight() * cos + BI_VEHICLE[i][0].getWidth() * sin);
                    BI_VEHICLE[i][degree]= new BufferedImage(w, h, BI_VEHICLE[i][0].getType());
                    AffineTransform at = new AffineTransform();
                    at.translate(w / 2, h / 2);
                    at.rotate(Math.toRadians(degree),0, 0);
                    at.translate(-BI_VEHICLE[i][0].getWidth() / 2, -BI_VEHICLE[i][0].getHeight() / 2);
                    AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
                    rotateOp.filter(BI_VEHICLE[i][0],BI_VEHICLE[i][degree]);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static double MIN_FRONT_DIS = LENGTH[VT_MORTORBIKE];
    public static double MIN_SIDE_DIS = BREADTH[VT_MORTORBIKE];
    
    public static final int LT_RED=0;
    public static final int LT_YELLOW=1;
    public static final int LT_GREEN=2;
    public static String[] STR_LIGHT_NAMES=new String[]{"Red","Yellow","Green"};
    public static Color[] LIGHT_COLORS=new Color[]{Color.RED,Color.YELLOW,Color.GREEN};
    public static BufferedImage[] BI_LIGHT=new BufferedImage[STR_LIGHT_NAMES.length];

    public static double LIGHT_IMAGE_BREADTH = 15;
    public static double LIGHT_IMAGE_LENGTH = 30;

    public static final String FILE_EXT=".tsd";
    
    public static double EPS_LINE=5;
    public static double EPS_POINT=5;
}
