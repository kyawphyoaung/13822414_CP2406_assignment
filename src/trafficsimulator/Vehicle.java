package trafficsimulator;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Vector;

public final class Vehicle {
    private long id;
    private int type;
    public double speed=0.0;
    public double maxSpeed;
    private Polyline pathLine;
    
    private int curSegId=0;
    private double curSegX=0.0;
    public Vector2 curPos=new Vector2();
    public Vector2 curDir=new Vector2();
    public Vector<Vector2> points=new Vector<>(4);
    private Vector2 leftTop=new Vector2(-1000,-1000);
    private Vector2 widthHeight=new Vector2(0,0);
    
    public double totalTime=0.0;
    public double totalLenth=0.0;
    
    Point csLight=null;
    
    public Vehicle(int type, Polyline pathLine, double speed){
        for(int i=0;i<4;i++){
            points.addElement(new Vector2(-1000,-1000));
        }
        id=System.currentTimeMillis();
        setAll(type, pathLine, speed);
    }
    public void setAll(int type, Polyline pathLine, double maxSpeed){
        this.type=type;
        this.pathLine=pathLine;
        this.maxSpeed=maxSpeed;
        calcRectForDraw();
    }
    public double bredth(){return Constants.BREADTH[type];}
    public double length(){return Constants.LENGTH[type];}
    public Vector2 getCurrentOriginPosition(){
        return pathLine.segFun(curSegId, curSegX);
    }
    public Vector2 getCurrentOriginDirect(){
        return pathLine.direct(curSegId, 0);
    }
    private BufferedImage getImage(){
        int degree=(int) Math.round(Math.toDegrees(curDir.angle()));
        if(degree<0)degree+=360;
        return Constants.BI_VEHICLE[type][degree];
    }
    public void incSpeed(){
        speed=speed+1;
        if(speed>maxSpeed)speed=maxSpeed;
    }
    public void decSpeed(){
        speed=speed-1;
        if(speed<0)speed=0;
    }
    private void calcRectForDraw(){
        curPos=pathLine.segFunSmooth(curSegId, curSegX, length()/2);
        Vector2 vD=pathLine.directSmooth(curSegId,curSegX, 0, length()/2);
        curDir.set(vD);
        Vector2 vN=vD.rotLeft90New();
        vD.mult(length()/2);
        vN.mult(bredth()/2);
        points.elementAt(0).set(curPos.addNew(vD).addNew(vN));
        points.elementAt(1).set(curPos.subNew(vD).addNew(vN));
        points.elementAt(2).set(curPos.subNew(vD).subNew(vN));
        points.elementAt(3).set(curPos.addNew(vD).subNew(vN));
        leftTop.set(points.firstElement());
        widthHeight.set(points.firstElement());
        for (Vector2 point : points) {
            if(leftTop.x>point.x)leftTop.x=point.x;
            if(leftTop.y>point.y)leftTop.y=point.y;
            if(widthHeight.x<point.x)widthHeight.x=point.x;
            if(widthHeight.y<point.y)widthHeight.y=point.y;
        }
        widthHeight.sub(leftTop);
    }
    public void draw(Graphics g, double scale){
        if(isOutTartget())return;
        BufferedImage bufImage=getImage();
        if(bufImage==null){
            Vector2 vPre=points.lastElement();
            for (Vector2 point : points) {
                g.drawLine((int) Math.round(scale*vPre.x),
                        (int) Math.round(scale*vPre.y),
                        (int) Math.round(scale*point.x), 
                        (int) Math.round(scale*point.y));
                vPre=point;
            }
        }else{
            g.drawImage(bufImage,
                    (int) Math.round(scale*leftTop.x),
                    (int) Math.round(scale*leftTop.y),
                    (int) Math.round(scale*widthHeight.x),
                    (int) Math.round(scale*widthHeight.y) ,
                    null);
        }
    }
    public void move(double time){
        if(isOutTartget())return;
        totalTime+=Math.abs(time);
        double ds=speed*time;
        totalLenth+=ds;
        curSegX+=ds;
        while(0<=curSegId && curSegId<pathLine.pointCount()-1){
            double len=pathLine.getSegLen(curSegId);
            if(curSegX>=len){
                curSegX-=len;curSegId++;
            }else if(curSegX<0.0){
                curSegId--;curSegX+=pathLine.getSegLen(curSegId);
            }else{
                break;
            }
        }
        if(curSegId<0){
            curSegId=0;curSegX=0.0;
        }
        calcRectForDraw();
    }
    public boolean isOutTartget(){
        return curSegId>pathLine.pointCount()-2;
    }
    private Vector2 minDis(Vehicle vhc){
        Vector2 vP=curPos;//getCurrentOriginPosition();
        Vector2 minV=vhc.curPos.subNew(vP);
        Vector2 vD=curDir;//getCurrentOriginDirect();
        Vector2 res=new Vector2(vD.sPrd(minV)-(length()+vhc.length())/2,
                            Math.abs(vD.rotLeft90New().sPrd(minV))-(bredth()+vhc.bredth())/2);
        if(res.y<0)res.y=0;
        return res;
    }
    public boolean checkLight(Light light,double dtime, double roadBreadth){
        if(light.state==Constants.LT_RED){
            Vector2 vD=getCurrentOriginDirect();
            Vector2 vN=vD.rotLeft90New();
            double vprd=vD.vPrd(light.direct);
            double sprd=vD.sPrd(light.direct);
            if(Math.abs(vprd)<0.1 && sprd< 1.0-0.1){
                double xdis=light.direct.sPrd(curPos.subNew(light.position))-length()/2-roadBreadth/8;
                double ydis=vN.sPrd(light.position.subNew(curPos));
                if(-roadBreadth/8< xdis && xdis<speed*dtime*2 && 0.0<=ydis && ydis<=roadBreadth/2+Constants.LIGHT_IMAGE_BREADTH)return false;
            }
        }
        return true;
    }
    public boolean checkLights(Vector< Vector<Light> > vecLights, double dtime, double roadBreadth){
        for (Vector<Light> vecLight : vecLights) {
            for (Light light : vecLight) {
                if(!checkLight(light, dtime, roadBreadth))return false;
            }
        }
        return true;
    }
    public boolean checkPosition(Vector<Vehicle> vecVhc, Vector< Vector<Light> > vecLights){
        for (Vehicle vhc : vecVhc) {
            if(this==vhc)continue;
            Vector2 disV=minDis(vhc);
            if(Polyline.isIntersect(points, vhc.points))return false;
            if(disV.x>0.0 && disV.x<Constants.MIN_FRONT_DIS && disV.y<Constants.MIN_SIDE_DIS)return false;
        }
        return true;
    }
    public boolean isValid(Vector<Vehicle> vecVhc){
        for (Vehicle vhc : vecVhc) {
            if(this==vhc)continue;
            if(Polyline.isIntersect(points, vhc.points))return false;
            if(Polyline.minDisVector(points, vhc.points).len()<Constants.MIN_SIDE_DIS)return false;
        }
        return true;
    }    
}
