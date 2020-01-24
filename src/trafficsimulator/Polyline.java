
package trafficsimulator;

import java.awt.Graphics2D;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Vector;

public class Polyline {
    Vector<Vector2> points;
    public Polyline(){
        points=new Vector<>();
    }
    public int pointCount(){
        return points.size();
    }
    public double getSegLen(int i){
        Vector2 v1=points.elementAt(i);
        Vector2 v2=points.elementAt((i+1)%pointCount());
        return v2.subNew(v1).len();
    }
    public Vector2 segFun(int i, double x){
        Vector2 v1=points.elementAt(i);
        Vector2 v2=points.elementAt((i+1)%pointCount());
        return v1.addNew(v2.subNew(v1).unitNew().multNew(x));
    }
    public Vector2 segFunSmooth(int segId, double segX, double deltaLen){
        Vector2 vBack=new Vector2(), vFront=new Vector2();
        if(getSmoothPoints(segId, segX, vBack, vFront, deltaLen)){
            return vFront.addNew(vBack).multNew(0.5);
        }
        return segFun(segId, segX);
    }
    private boolean getSmoothPoints(int segId,double segX, Vector2 vBack, Vector2 vFront, double deltaLen){
        double curLen=getSegLen(segId);
        if(deltaLen>curLen/3)deltaLen=curLen/3;
        if(segId>0){
            double preLen=getSegLen(segId-1);
            if(deltaLen>preLen/3)deltaLen=preLen/3;
            if(segX<deltaLen){
                vBack.set(segFun(segId-1, preLen+(segX-deltaLen)));
                vFront.set(segFun(segId, segX+deltaLen));
                return true;
            }
        }
        if(segId<pointCount()-2){
            double nextLen=getSegLen(segId+1);
            if(deltaLen>nextLen/3)deltaLen=nextLen/3;            
            double dlen=segX+deltaLen-curLen;
            if(dlen>0.0){
                vFront.set(segFun(segId+1, dlen));
                vBack.set(segFun(segId, segX-deltaLen));
                return true;
            }
        }
        return false;
    }
    public Vector2 directSmooth(int segId,double segX, int startEnd, double deltaLen){
        Vector2 vBack=new Vector2(), vFront=new Vector2();
        if(getSmoothPoints(segId, segX, vBack, vFront, deltaLen)){
            if(startEnd==0){
                return vFront.subNew(vBack).unitNew();
            }else{
                return vBack.subNew(vFront).unitNew();
            }
        }
        return points.elementAt(segId+1).subNew(points.elementAt(segId)).unitNew();
    }
    public Vector2 direct(int segId, int startEnd){
        if(startEnd==0){
            return points.elementAt(segId+1).subNew(points.elementAt(segId)).unitNew();
        }else{
            return points.elementAt(segId-1).subNew(points.elementAt(segId)).unitNew();
        }
    }
    public int possibleAddingPre(){
        if(pointCount()>1){
            if(points.lastElement().dis(points.elementAt(points.size()-2))<0.1)return 0;
        }
        if(pointCount()>2){
            Vector2 v1=points.elementAt(pointCount()-3);
            Vector2 v2=points.elementAt(pointCount()-2);
            if(points.lastElement().disLine1(v1, v2)<1)return 0;
            if(points.lastElement().disLine(v1, v2)<1)return 2;
        }
        return 1;
    }
    public void setPrePoint(Vector2 v){
        if(points.size()==0)
            points.addElement(v);
        else
            points.lastElement().set(v);
    }
    public void addPointForInput(int resSelfLinePossible){
        if(resSelfLinePossible==1)
            points.addElement(new Vector2(points.lastElement()));
        else if(resSelfLinePossible==2){
            points.elementAt(pointCount()-2).set(points.lastElement());
        }
    }
    public void addPointEnd(){
        points.removeElementAt(points.size()-1);
    }
    public boolean checkIntersectPre(){
        for(int i=0;i<pointCount()-3;i++){
            if(Vector2.intersect(points.elementAt(i), points.elementAt(i+1), points.elementAt(pointCount()-2), points.elementAt(pointCount()-1),0.01))return false;
        }
        return true;
    }
    public boolean checkIntersectPre(Polyline line){
        if(line==this)return checkIntersectPre();
        if(pointCount()<2)return true;
        for(int i=0;i<line.pointCount()-1;i++){
            if(Vector2.intersect(line.points.elementAt(i), line.points.elementAt(i+1), points.elementAt(pointCount()-2), points.elementAt(pointCount()-1),0.01))return false;
        }
        return true;
    }
    public boolean checkIntersect(){
        for(int i=0;i<pointCount()-3;i++){
            for(int j=i+2;j<pointCount()-1;j++){
                if(Vector2.intersect(points.elementAt(i), points.elementAt(i+1), points.elementAt(j), points.elementAt(j+1),0.01))return false;
            }
        }
        return true;
    }
    public boolean checkIntersect(Polyline line){
        if(line==this)return checkIntersect();
        for(int i=0;i<pointCount()-1;i++){
            for(int j=0;j<line.pointCount()-1;j++){
                if(Vector2.intersect(points.elementAt(i), points.elementAt(i+1), line.points.elementAt(j), line.points.elementAt(j+1),0.01))return false;
            }
        }
        return true;
    }
    public Polyline split(int segId, double segX){
        Polyline line=new Polyline();line.points.removeAllElements();
        Vector2 v0;
        if(segX==0.0){
            if(segId>=pointCount())return null;
            if(segId==0)return null;
            v0=new Vector2(points.elementAt(segId));
        }else{
            v0=new Vector2(segFun(segId, segX));
        }
        line.points.addElement(v0);
        while(pointCount()>segId+1){
            line.points.addElement(points.elementAt(segId+1));
            points.removeElementAt(segId+1);
        }
        if(segX>0.0){
            points.addElement(new Vector2(v0));
        }
        return line;
    }
    public boolean findNearLinePointIX(Vector2 v, Vector2 res){
        for(int i=0;i<pointCount()-1;i++){
            Vector2 v1=points.elementAt(i);
            Vector2 v2=points.elementAt((i+1)%pointCount());
            if(v.disLine1(v1, v2)<Constants.EPS_LINE){
                Vector2 dv=v2.subNew(v1);
                double subLen=dv.len();
                dv.unit();
                double xs=dv.sPrd(v.subNew(v1));
                if(xs<Constants.EPS_POINT)xs=0.0;
                if(xs>=subLen-Constants.EPS_POINT){
                    xs=0.0;
                    i++;
                }
                res.set(i, xs);
                return true;
            }
        }
        return false;
    }
    public void findNearControlPointIIs(Vector2 v, int myId, Vector<Point> res){
        
        for(int i=0;i<pointCount();i++){
            if(v.dis(points.elementAt(i))<Constants.EPS_POINT){
                res.add(new Point(myId, i));
            }           
        }
    }
    
    public Polyline makeSide(double roadBreadthHalf){
        Polyline line=new Polyline();
        Vector2 vM = new Vector2(),vd1d2 = new Vector2();
        for(int i=0;i<points.size()-1;i++){
            Vector2 v1=points.elementAt(i);
            Vector2 v2=points.elementAt(i+1);
            Vector2 dv=v2.subNew(v1);
            dv.unit();
            Vector2 vn=dv.rotLeft90New().multNew(roadBreadthHalf);
            Vector2 vS1=v1.addNew(vn);
            Vector2 vS2=v2.addNew(vn);
            if(i==0){
                line.points.addElement(vS1);
            }else{
                Vector2 vS1Pre=line.points.elementAt(line.points.size()-2);
                Vector2 vS2Pre=line.points.lastElement();
                double s=Vector2.intersect(vS1Pre, vS2Pre, vS1, vS2, vM, vd1d2);
                if(s!=0.0){
                    line.points.lastElement().set(vM);
                }
            }
            line.points.addElement(vS2);
        }
        return line;
    }
    public int reduceNearPoints(){
        int rn=0;
        for(int i=0;i<pointCount()-2;i++){
            if(points.elementAt(i+1).disLine1(points.elementAt(i), points.elementAt(i+2))<Constants.EPS_LINE){
                points.removeElementAt(i+1);
                rn++;
            }
        }
        return rn;
    }
    public Vector2 getTerminalDirect(int startEnd){
        if(pointCount()<2)return new Vector2();
        if(startEnd==0){
            return points.elementAt(1).subNew(points.firstElement()).unitNew();
        }else{
            return points.elementAt(pointCount()-2).subNew(points.lastElement()).unitNew();
        }
    }
    public Vector2 getTerminalPoint(int startEnd){
        if(pointCount()<2)return new Vector2();
        if(startEnd==0){
            return points.firstElement();
        }else{
            return points.lastElement();
        }
    }
    
    public void moveDelta(Vector2 vDelta){
        for (Vector2 point : points) {
            point.add(vDelta);
        }
    }
    public void draw(Graphics2D g2d, double scale){
        if(points.size()>0){
            for(int i=0;i<points.size()-1;i++){
                Vector2 v1=points.elementAt(i);
                Vector2 v2=points.elementAt(i+1);
                g2d.drawLine((int)Math.round(scale*v1.x), (int)Math.round(scale*v1.y), (int)Math.round(scale*v2.x), (int)Math.round(scale*v2.y));
            }
        }
    }
    public void drawPoints(Graphics2D g2d,double scale){
        for (int i=0;i<pointCount();i++) {
            Vector2 v=points.elementAt(i);
            double r=Constants.EPS_POINT;
//            if(i==0 || i==pointCount()-1)r=r*2;
            g2d.drawRoundRect((int)Math.round(scale*(v.x-r)),(int)Math.round(scale*(v.y-r)),(int)Math.round(r*2*scale), (int)Math.round(r*2*scale), (int)Math.round(r*scale), (int)Math.round(r*scale));    
        }
    }
    
   public boolean save(PrintWriter pw){
        try{
            pw.println("    Begin_Line");
            for(int i=0;i<pointCount();i++){
                Vector2 v=points.elementAt(i);
                pw.println("      xy="+v.x+" , "+v.y);
            }
            pw.println("    End_Line");
            return true;
        }catch(Exception ex){}
        return false;
    }
    public boolean load(BufferedReader br){
        try{
            points.removeAllElements();
            for(int repeat=0;repeat<1000;repeat++){
                String line=br.readLine();
                if(line==null)return false;
                line=line.trim();
                if(line.equals(""))continue;
                if("End_Line".equalsIgnoreCase(line)){
                    return true;
                }
                int k=line.indexOf("=");
                if(k<0)continue;
                String paramName=line.substring(0,k).trim();
                String paramValue=line.substring(k+1).trim();
                if("xy".equals(paramName)){
                    k=paramValue.indexOf(",");
                    Vector2 v=new Vector2(Double.parseDouble(paramValue.substring(0, k).trim()),Double.parseDouble(paramValue.substring(k+1).trim()));
                    points.addElement(v);
                }
            }
        }catch(Exception ex){}
        return false;
    }
    
    public static Vector2 minDisVector(Vector<Vector2> points1, Vector<Vector2> points2){
        Vector2 minDisV=new Vector2(-1000000.0,-1000000.0);
        double minDis=minDisV.len();
        for(Vector2 p1: points1){
            Vector2 disV=minDisVector(p1, points2);
            double dis=disV.len();
            if(minDis>dis){
                minDis=dis;
                minDisV=disV;
            }
        }
        for(Vector2 p2: points2){
            Vector2 disV=minDisVector(p2, points1);
            double dis=disV.len();
            if(minDis>dis){
                minDis=dis;
                minDisV.set(-disV.x,-disV.y);
            }
        }
        return minDisV;
    }
    public static Vector2 minDisVector(Vector2 v0, Vector<Vector2> points){
        Vector2 minDisV=new Vector2(-1000000.0,-1000000.0);
        double minDis=minDisV.len();
        Vector2 pPre=points.lastElement();
        for (Vector2 p : points) {
            Vector2 disV;
            Vector2 dpPreP=p.subNew(pPre);
            double len=dpPreP.len();
            dpPreP.mult(1.0/len);            
            double lenx=dpPreP.sPrd(v0.subNew(pPre));
            if(0.0 < lenx && lenx < len){
                Vector2 p0=pPre.addNew(dpPreP.multNew(lenx));
                disV=p0.subNew(v0);
            }else{
                disV=p.subNew(v0);
            }
            len=disV.len();
            if(len<minDis){
                minDis=len;
                minDisV=disV;
            }
            pPre=p;
        }
        return minDisV;
    }
    public static boolean isIntersect(Vector<Vector2> points1, Vector<Vector2> points2){
        Vector2 p1Pre=points1.lastElement();
        for (Vector2 p1 : points1) {
            if(isPtInPolygon(p1, points2))return true;
            if(isPtInPolygon(p1.addNew(p1Pre).multNew(0.5), points2))return true;
            p1Pre=p1;
        }
        Vector2 p2Pre=points2.lastElement();
        for (Vector2 p2 : points2) {
            if(isPtInPolygon(p2.addNew(p2Pre).multNew(0.5), points1))return true;
            p2Pre=p2;
        }
        p1Pre=points1.lastElement();
        for (Vector2 p1 : points1) {
            p1Pre=p1;
            p2Pre=points2.lastElement();
            for (Vector2 p2 : points2) {
                if(Vector2.intersect(p1, p1Pre, p2, p2Pre, 0.0))return true;
                p2Pre=p2;
            }
        }
        return false;
    }
    public static boolean isPtInPolygon(Vector2 v0, Vector<Vector2> points){
        Vector2 vd1d2=new Vector2();
        for(double al=0.0;al<Math.PI;al+=0.1){
            Vector2 vDir=new Vector2(Math.cos(al), Math.sin(al));
            int i1,i2;
            int cntPos=0,cntNeg=0;
            double s=0.0;
            for(i1=0;i1<points.size();i1++){
                i2=(i1+1)%points.size();
                s=Vector2.intersect(v0, v0.addNew(vDir), points.elementAt(i1), points.elementAt(i2), null, vd1d2);
                if(s==0.0)break;
                if(vd1d2.x==0.0 || vd1d2.x==1.0)break;
                if(vd1d2.x>0.0 && vd1d2.x<1.0){
                    if(vd1d2.y>0)cntPos=1-cntPos;
                    if(vd1d2.y<0)cntNeg=1-cntNeg;
                }
            }
            if(i1==points.size()){
                if(cntPos==1 && cntNeg==1)return true;
            }
        }
        return false;
    }    
}
