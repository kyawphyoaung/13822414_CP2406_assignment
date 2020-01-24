
package trafficsimulator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Vector;

public class RoadMap {
    double roadBreadth=Constants.ROAD_BREADTH;
    Vector< Polyline> midLines=new Vector<>();
    Vector< Polyline > leftLines=new Vector<>();
    Vector< Polyline > rightLines=new Vector<>();
    Vector<Vector<Point> > vecTermLses=new Vector<>();
    
    public RoadMap(){
        midLines.addElement(new Polyline());
    }
    public int lineCount(){
        return midLines.size()-1;
    }
    public Polyline currentMidLine(){
        return midLines.lastElement();
    }
    public Polyline currentLeftLine(){
        return leftLines.lastElement();
    }
    public Polyline currentRightLine(){
        return rightLines.lastElement();
    }
    
    private void makeSide(double roadBreadthHalf, Vector<Polyline> side){
        side.removeAllElements();
        for (Polyline midLine : midLines) {
            Polyline line=midLine.makeSide(roadBreadthHalf);
            side.addElement(line);
        }
    }
    private Vector2 getTerminalPoint(Point lse){Polyline line=midLines.elementAt(lse.x);return line.getTerminalPoint(lse.y);}
    private Vector2 getTerminalDirect(Point lse){Polyline line=midLines.elementAt(lse.x);return line.getTerminalDirect(lse.y);}
    private double getTerminalAngle(Point lse){return getTerminalDirect(lse).angle();}
    private void addPointToTermConn(Point lse){
        int i;
        for(i=0;i<vecTermLses.size();i++){
            Vector<Point> termLses=vecTermLses.elementAt(i);
            int j=0;
            for(j=0;j<termLses.size();j++){
                if(getTerminalPoint(lse).dis(getTerminalPoint(termLses.elementAt(j)))<Constants.EPS_POINT*1.5)break;
            }
            if(j<termLses.size()){//add by sorting
                for(j=0;j<termLses.size();j++){
                    if(getTerminalAngle(lse)<getTerminalAngle(termLses.elementAt(j)))break;
                }
                termLses.insertElementAt(lse,j);
                return;
            }
        }
        Vector<Point> termLses=new Vector<>();
        termLses.addElement(lse);
        vecTermLses.addElement(termLses);
    }
    private void makeTermLses(){
        vecTermLses.removeAllElements();
        conn=null;
        for(int i=0;i<midLines.size();i++) {
            if(midLines.elementAt(i).pointCount()>1){
                for(int k=0;k<2;k++){
                    addPointToTermConn(new Point(i, k));
                }
            }
        }
        for (Vector<Point> termLses : vecTermLses) {
            Vector2 v=new Vector2();
            for (Point ii : termLses) {
                v.add(getTerminalPoint(ii));
            }
            v.mult(1.0/termLses.size());
            for (Point ii : termLses) {
                getTerminalPoint(ii).set(v);
            }
        }
    }
    private void makeCrossSide(){
        for (Vector<Point> termLses : vecTermLses) {
            if(termLses.size()>1){
                for(int i1=0;i1<termLses.size();i1++){
                    int i2=(i1+1)%termLses.size();
                    Polyline line1=null, line2=null;
                    int startEnd1=termLses.elementAt(i1).y;
                    int startEnd2=termLses.elementAt(i2).y;
                    if(startEnd1==0){//start direct
                        line1=rightLines.elementAt(termLses.elementAt(i1).x);
                    }else{
                        line1=leftLines.elementAt(termLses.elementAt(i1).x);
                    }
                    if(startEnd2==0){//start direct
                        line2=leftLines.elementAt(termLses.elementAt(i2).x);
                    }else{
                        line2=rightLines.elementAt(termLses.elementAt(i2).x);
                    }
                    Vector2 dv1=line1.getTerminalDirect(startEnd1);
                    Vector2 tv1=line1.getTerminalPoint(startEnd1);
                    Vector2 dv2=line2.getTerminalDirect(startEnd2);
                    Vector2 tv2=line2.getTerminalPoint(startEnd2);
                    Vector2 rM=new Vector2();
                    double s=Vector2.intersect(tv1, tv1.addNew(dv1), tv2, tv2.addNew(dv2), rM, null);
                    if(s!=0.0){
                        tv1.set(rM);
                        tv2.set(rM);
                    }
                }
            }
        }
    }
    private void mergeTwoCross(){
        boolean flagChanged=true;
        while(flagChanged){
            flagChanged=false;
            makeTermLses();
            for (Vector<Point> termLses : vecTermLses) {
                if(termLses.size()==2){
                    if(termLses.firstElement().x!=termLses.lastElement().x){
                        Polyline newLine=midLines.elementAt(termLses.firstElement().x);
                        int newStartEnd=termLses.firstElement().y;
                        Polyline addedLine=midLines.elementAt(termLses.lastElement().x);
                        int addedStartEnd=termLses.lastElement().y;
                        if(newStartEnd==1 && addedStartEnd==0){
                            for(int i=1;i<addedLine.pointCount();i++)
                                newLine.points.addElement(addedLine.points.elementAt(i));
                        }
                        if(newStartEnd==1 && addedStartEnd==1){
                            for(int i=addedLine.pointCount()-2;i>=0;i--)
                                newLine.points.addElement(addedLine.points.elementAt(i));
                        }
                        if(newStartEnd==0 && addedStartEnd==0){
                            for(int i=1;i<addedLine.pointCount();i++)
                                newLine.points.insertElementAt(addedLine.points.elementAt(i),0);
                        }
                        if(newStartEnd==0 && addedStartEnd==1){
                            for(int i=addedLine.pointCount()-2;i>=0;i--)
                                newLine.points.insertElementAt(addedLine.points.elementAt(i),0);
                        }
                        midLines.removeElement(addedLine);
                        flagChanged=true;
                        break;
                    }
                }
            }
        }
    }
    private void removeRestLines(){
        Vector<Polyline> restLines=new Vector<>();
        for(int i=0;i<midLines.size()-1;i++){
            if(midLines.elementAt(i).pointCount()<2)restLines.addElement(midLines.elementAt(i));
        }
        for(Polyline line: restLines){
            midLines.removeElement(line);
        }
    }
    public void makeSide(){
        makeTermLses();
        makeSide(roadBreadth/2,leftLines);
        makeSide(-roadBreadth/2,rightLines);
        makeCrossSide();
    }
    public void reducePoints(){
        for (Polyline line : midLines) {
            line.reduceNearPoints();
        }
        for (Polyline line : leftLines) {
            line.reduceNearPoints();
        }
        for (Polyline line : rightLines) {
            line.reduceNearPoints();
        }
    }
    public int findRoadIncluding(Vector2 v0){
        Vector<Vector2> pts=new Vector<>();
        for(int i=0;i<midLines.size()-1;i++){
            pts.removeAllElements();
            for (int j=0;j<leftLines.elementAt(i).points.size();j++) {
                pts.addElement(leftLines.elementAt(i).points.elementAt(j));
            }
            for (int j=rightLines.elementAt(i).points.size()-1;j>=0;j--) {
                pts.addElement(rightLines.elementAt(i).points.elementAt(j));
            }
            if(Polyline.isPtInPolygon(v0, pts))return i;
        }
        return -1;
    }
    public void findRoad(int lineId, Vector<Integer> vecLineIds){
        if(vecLineIds.indexOf(lineId)<0)
            vecLineIds.addElement(lineId);
        while(true){
            boolean changed=false;
            for (Vector<Point> termLses : vecTermLses) {
                if(termLses.size()>1){
                    int i=0;
                    for(i=0;i<termLses.size();i++){
                        if(vecLineIds.indexOf(termLses.elementAt(i).x)>=0)break;
                    }
                    if(i<termLses.size()){
                        for (Point term : termLses) {
                            if(vecLineIds.indexOf(term.x)<0){
                                vecLineIds.addElement(term.x);
                                changed=true;
                            }
                        }
                    }
                }
            }
            if(!changed)break;
        }
    }
    public void moveRoad(int lineId, Vector2 vDelta){
        Vector<Integer> vecLineIds=new Vector<>();
        findRoad(lineId, vecLineIds);
        for (Integer lId : vecLineIds) {
            midLines.elementAt(lId).moveDelta(vDelta);
            leftLines.elementAt(lId).moveDelta(vDelta);
            rightLines.elementAt(lId).moveDelta(vDelta);
        }
    }
    public void findTerminalLSEs(Vector<Point> vecLse){
        for (Vector<Point> termLses : vecTermLses) {
            if(termLses.size()==1){
                vecLse.add(termLses.firstElement());
            }
        }
    }
    public void deleteRoad(int lineId){
        Vector<Integer> vecLineIds=new Vector<>();
        findRoad(lineId, vecLineIds);
        Vector<Polyline> delLines=new Vector<>();
        for (Integer lId : vecLineIds) {
            delLines.addElement(midLines.elementAt(lId));
        }
        for (Polyline delLine : delLines) {
            midLines.removeElement(delLine);
        }
        makeSide();
    }
    public int findNearLinePointIX(Vector2 v, Vector2 res){
        for(int i=0;i<midLines.size()-1;i++){
            Polyline line=midLines.elementAt(i);
            if(line.findNearLinePointIX(v, res))return i;
        }
        return -1;
    }
    public void findNearControlPointIIs(Vector2 v, Vector<Point> res){
        for(int i=0;i<midLines.size()-1;i++){
            Polyline line=midLines.elementAt(i);
            line.findNearControlPointIIs(v, i, res);
        }
    }
    public void setPrePoint(Vector2 v){
        currentMidLine().setPrePoint(v);
    }
    public void addPointForInput(int resSelfLinePossible){
        currentMidLine().addPointForInput(resSelfLinePossible);
        if(resSelfLinePossible!=0){
            makeSide();
        }
    }
    public void makeAll(){
        makeTermLses();
        mergeTwoCross();
        makeSide(roadBreadth/2,leftLines);
        makeSide(-roadBreadth/2,rightLines);
        makeCrossSide();
        reducePoints();
        makeConnAtTerm();
    }
    public boolean addPointEnd(){
        if(currentMidLine().pointCount()>2){
            currentMidLine().addPointEnd();
            makeAll();
            midLines.addElement(new Polyline());
            return true;
        }else{
            currentMidLine().points.removeAllElements();
            return false;
        }
        
    }
    public int possibleAddingPre(){
        return currentMidLine().possibleAddingPre();
    }
    public boolean checkIntersect(Vector<Polyline> lines){
        for (Polyline line : midLines) {
            for (Polyline line1 : lines) {
                if(!line.checkIntersect(line1))return false;
            }
        }
        for (Polyline line : leftLines) {
            for (Polyline line1 : lines) {
                if(!line.checkIntersect(line1))return false;
            }
        }
        for (Polyline line : rightLines) {
            for (Polyline line1 : lines) {
                if(!line.checkIntersect(line1))return false;
            }
        }
        return true; 
    }
    public boolean checkIntersect(){
        if(!checkIntersect(midLines))return false;
        if(!checkIntersect(rightLines))return false;
        if(!checkIntersect(leftLines))return false;
        return true; 
    }
    public boolean checkIntersectPre(){
        for (Polyline line : midLines) {
            if(!currentMidLine().checkIntersectPre(line))return false;
            if(!currentLeftLine().checkIntersectPre(line))return false;
            if(!currentRightLine().checkIntersectPre(line))return false;
        }
        for (Polyline line : leftLines) {
            if(!currentMidLine().checkIntersectPre(line))return false;
            if(!currentLeftLine().checkIntersectPre(line))return false;
            if(!currentRightLine().checkIntersectPre(line))return false;
        }
        for (Polyline line : rightLines) {
            if(!currentMidLine().checkIntersectPre(line))return false;
            if(!currentLeftLine().checkIntersectPre(line))return false;
            if(!currentRightLine().checkIntersectPre(line))return false;
        }
        return true;
    }
    public boolean split(int lineId, int segId, double segX){
        Polyline line=midLines.elementAt(lineId).split(segId, segX);
        if(line!=null){
            midLines.insertElementAt(line, midLines.size()-2);
            return true;
        }
        return true;
    }
    public int getRoadCount(){
        return midLines.size()-1;
    }
    public void draw(Graphics g, double scale,boolean isCtrl){
        Graphics2D g2d = (Graphics2D) g.create();
        for (Polyline line : leftLines) {
            line.draw(g2d, scale);
        }
        for (Polyline line : rightLines) {
            line.draw(g2d, scale);
        }
        if(isCtrl){
            for (Polyline line : midLines) {
                line.drawPoints(g2d, scale);
            }
        }         
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(dashed);        
        for (Polyline line : midLines) {
            line.draw(g2d, scale);
        }
        g2d.dispose();
    }
    public boolean save(PrintWriter pw){
        try{
            pw.println("  Begin_RoadMap");
            for (Polyline line : midLines) {
                if(!line.save(pw))return false;
            }
            pw.println("  End_RoadMap");
            return true;
        }catch(Exception ex){}
        return false;
    }
    public boolean load(BufferedReader br){
        try{
            midLines.removeAllElements();
            for(int repeat=0;repeat<1000;repeat++){
                String line=br.readLine();
                if(line==null)return false;
                line=line.trim();
                if(line.equals(""))continue;
                if("End_RoadMap".equalsIgnoreCase(line)){
                    makeSide();
                    return true;
                }
                if("Begin_Line".equalsIgnoreCase(line)){
                    Polyline polyline=new Polyline();
                    if(!polyline.load(br))return false;
                    midLines.addElement(polyline);
                }
            }
        }catch(Exception ex){}
        return false;
    }
    
    
    private Vector<Point> findLsesAtTermLses(int startTId, int destTId){
        Vector<Point> res=new Vector<>();
        for (Point lseS : vecTermLses.elementAt(startTId)) {
            for (Point lseD : vecTermLses.elementAt(destTId)) {
                if(lseS.x==lseD.x)res.addElement(lseS);
            }
        }
        return res;
    }
    
    
    private int[][] conn=null;
    private int[][] connExpand=null;
    private void makeConnAtTerm(){
        conn=new int[vecTermLses.size()][vecTermLses.size()];
        connExpand=new int[vecTermLses.size()][vecTermLses.size()];
        for(int i=0;i<conn.length;i++){
            for(int j=0;j<conn.length;j++){
                if(i==j)continue;
                connExpand[i][j]=conn[i][j]=findLsesAtTermLses(i, j).size();
            }
        }
        boolean fChanged=true;
        while (fChanged) {
            fChanged=false;
            for(int i=0;i<conn.length;i++){
                for(int j=0;j<conn.length;j++){
                    if(i==j)continue;
                    if(connExpand[i][j]==0){
                        for(int k=0;k<conn.length;k++){
                            if(k==i || k==j)continue;
                            if(connExpand[i][k]!=0 && connExpand[k][j]!=0){
                                connExpand[i][j]=-1;
                                fChanged=true;
                                break;
                            }
                        }
                    }
                }
            }
        }        
    }
    private int findRandomPathAtTermLses(int startTId, int endTId,boolean[] visited,int[] path, int n){
        visited[startTId]=true;
        path[n]=startTId;
        if(startTId==endTId)return n;
        boolean[] cf=new boolean[conn.length];
        for(int i=0;i<conn.length;i++){
            int nextTId;
            do{
                nextTId=(int) (Math.random()*conn.length);
            }while(cf[nextTId]);
            cf[nextTId]=true;
            if(!visited[nextTId] && conn[startTId][nextTId]!=0){
                int rn=findRandomPathAtTermLses(nextTId, endTId, visited, path, n+1);
                if(rn>0)return rn;
            }
        }
        visited[startTId]=false;
        return 0;
    }
    public Polyline makeRandomPath(int vehicleType){
        if(conn==null)
            makeConnAtTerm();     
        Vector<Integer> _terminals=new Vector<>();
        for(int i=0;i<vecTermLses.size();i++){
            if(vecTermLses.elementAt(i).size()==1){
                _terminals.add(i);
            }
        }
        Vector<Integer> terminals=new Vector<>();
        for(Integer ii: _terminals){
            int n=0;
            for(int j=0;j<conn.length;j++){
                if(connExpand[ii][j]!=0)n++;
            }
            if(n>0){
                terminals.addElement(ii);
            }
        }
        if(terminals.size()<2)return null;
        int kk=(int) (Math.random()*terminals.size());
        int startTId=terminals.elementAt(kk);
        int endTId=0;
        do{
            kk=(int) (Math.random()*terminals.size());
            endTId=terminals.elementAt(kk);
        }while(endTId==startTId || connExpand[startTId][endTId]==0);
        int[] pathTIds=new int[conn.length];
        boolean[] visited=new boolean[conn.length];
        int numPath=findRandomPathAtTermLses(startTId, endTId, visited, pathTIds, 0);
        if(numPath<=0)return null;
        Polyline pathLine=new Polyline();
        pathLine.points.addElement(getTerminalPoint(vecTermLses.elementAt(startTId).firstElement()));
        for(int i=1;i<=numPath;i++){
            Vector<Point> lses=findLsesAtTermLses(pathTIds[i-1], pathTIds[i]);
            int k=(int) (Math.random()*lses.size());
            Polyline line=midLines.elementAt(lses.elementAt(k).x);
            int startEnd=lses.elementAt(k).y;
            if(startEnd==0){
                for(int j=1;j<line.pointCount();j++){
                    pathLine.points.addElement(line.points.elementAt(j));
                }
            }else{
                for(int j=line.pointCount()-2;j>=0;j--){
                    pathLine.points.addElement(line.points.elementAt(j));
                }
            }
        }
        double widRest=roadBreadth/2-Constants.BREADTH[vehicleType]-Constants.MIN_SIDE_DIS-Constants.BREADTH[Constants.VT_BUS];
        if(widRest<0.0){
            widRest=0.0;
        }
        widRest=(Math.random()-0.5)*widRest+roadBreadth/4;
        return pathLine.makeSide(widRest);
    }
    public void makeLights(Vector<Vector<Light> > vecLights, Vector<Vector2> vecCrossPoint){
        for (Vector<Point> termLses : vecTermLses) {
            if(termLses.size()>2){
                vecCrossPoint.addElement(getTerminalPoint(termLses.firstElement()));
                Vector<Light> vecLight=new Vector<>();
                vecLights.addElement(vecLight);
                for(Point lse:termLses){
                    Polyline sideLine=null;
                    Polyline otherSideLine=null;
                    if(lse.y==0){//start direct
                        sideLine=rightLines.elementAt(lse.x);
                        otherSideLine=leftLines.elementAt(lse.x);
                    }else{
                        sideLine=leftLines.elementAt(lse.x);
                        otherSideLine=rightLines.elementAt(lse.x);
                    }
                    Vector2 tv=sideLine.getTerminalPoint(lse.y);
                    Vector2 tvOther=otherSideLine.getTerminalPoint(lse.y);
                    Vector2 dv=getTerminalDirect(lse);
                    Vector2 v0=getTerminalPoint(lse);
                    double x1=dv.sPrd(tv.subNew(v0));
                    double x2=dv.sPrd(tvOther.subNew(v0));
                    Vector2 pos=tv.addNew(tv.subNew(v0).unitNew().multNew(Constants.LIGHT_IMAGE_BREADTH));
                    if(x2>x1){
                        pos.add(dv.multNew(x2-x1));
                    }
                    Light light=new Light(dv, pos, 0);
                    vecLight.addElement(light);
                }
            }
        }
    }
}
