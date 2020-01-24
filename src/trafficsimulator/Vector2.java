
package trafficsimulator;

public final class Vector2 {
    public double x,y;
    public Vector2(){
        set(0.0,0.0);
    }
    public Vector2(Vector2 v){
        set(v);
    }
    public Vector2(double x, double y){
        set(x,y);
    }
    public void set(Vector2 v){
        set(v.x,v.y);
    }
    public void set(double x, double y){
        this.x=x;this.y=y;
    }
    public void add(Vector2 v){
        x+=v.x;y+=v.y;
    }
    public Vector2 addNew(Vector2 v){
        Vector2 vv=new Vector2(this);
        vv.add(v);
        return vv;
    }
    public void sub(Vector2 v){
        x-=v.x;y-=v.y;
    }
    public Vector2 subNew(Vector2 v){
        Vector2 vv=new Vector2(this);
        vv.sub(v);
        return vv;
    }    
    public void mult(double c){
        x*=c;y*=c;
    }
    public Vector2 multNew(double c){
        Vector2 vv=new Vector2(this);
        vv.mult(c);
        return vv;
    }    
    public void unit(){
        double l=len();
        if(l>0.0)mult(1.0/l);
    }
    public Vector2 unitNew(){
        Vector2 vv=new Vector2(this);
        vv.unit();
        return vv;
    }
    public Vector2 rotLeft90New(){
        Vector2 vv=new Vector2(y,-x);
        return vv;
    }
    public Vector2 rotRight90New(){
        Vector2 vv=new Vector2(-y,x);
        return vv;
    }
    public double sPrd(Vector2 v){
        return x*v.x+y*v.y;
    }
    public double vPrd(Vector2 v){
        return x*v.y-y*v.x;
    }
    public double len(){
        return Math.sqrt(sPrd(this));
    }
    public double dis(Vector2 v){
        return subNew(v).len();
    }
    public double disLine(Vector2 v1, Vector2 v2){
        Vector2 v1this=subNew(v1);
        Vector2 v1v2=v2.subNew(v1);
        double l12=v1v2.len();
        if(l12>0.0){
            return Math.abs(v1v2.vPrd(v1this))/l12;
        }else{
            return dis(v1);
        }
    }
    public double disLine1(Vector2 v1, Vector2 v2){
        Vector2 v1v2=v2.subNew(v1);
        Vector2 v1this=subNew(v1);
        Vector2 thisV2=v2.subNew(this);
        if(v1v2.sPrd(v1this)<0)return dis(v1);
        if(v1v2.sPrd(thisV2)<0)return dis(v2);
        double l12=v1v2.len();
        if(l12>0.0){
            return Math.abs(v1v2.vPrd(v1this))/l12;
        }else{
            return dis(v1);
        }
    }
    public double angle(){
        Vector2 u=unitNew();
        return Math.atan2(u.y, u.x);
    }
    public double angle(Vector2 v){
        Vector2 u1=unitNew();
        Vector2 u2=v.unitNew();
        double sinAl=u1.vPrd(u2);
        double cosAl=u1.sPrd(u2);
        return Math.atan2(sinAl, cosAl);
    }
    
    public static double intersect(Vector2 p1,Vector2 p2,Vector2 q1,Vector2 q2,Vector2 retM, Vector2 retd1d2){
        Vector2 p1p2=p2.subNew(p1);
        Vector2 q1q2=q2.subNew(q1);
        Vector2 q1p1=p1.subNew(q1);
        double s=p1p2.vPrd(q1q2);
        double s1=p1p2.vPrd(q1p1);
        double s2=q1q2.vPrd(q1p1);
        if(s!=0.0){
            s1/=s;s2/=s;
            if(retM!=null){
                retM.set(p1);
                p1p2.mult(s2);
                retM.add(p1p2);
            }
        }
        if(retd1d2!=null)retd1d2.set(s1, s2);
        return s;
    }
    public static boolean intersect(Vector2 p1,Vector2 p2,Vector2 q1,Vector2 q2, double eps){
        Vector2 vd1d2=new Vector2();
        double s=intersect(p1, p2, q1, q2, null,vd1d2);
        return s!=0.0 && eps<=vd1d2.x && vd1d2.x<1.0-eps && eps<=vd1d2.y && vd1d2.y<1.0-eps; 
    }
}
