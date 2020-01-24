package trafficsimulator;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Vector;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

public class CityPannel extends JPanel{
    static File tempFile;
    static{
        tempFile=new File("temp/");
        if(!tempFile.exists()){
            tempFile.mkdir();
        }
        File[] files=tempFile.listFiles();
        for (File file : files) {
            file.delete();
        }
    }
    public enum ModeType{
        MT_EDIT,
        MT_SINMULATE
    }
    
    private ModeType mode=ModeType.MT_EDIT;
    public enum EditType{
        ET_ADD,
        ET_EDIT
    }
    private EditType editMode=EditType.ET_ADD;
    
    
    public double scale=1.0;
      
    private final Cursor curcorCross=new Cursor(Cursor.CROSSHAIR_CURSOR);
    private final Cursor curcorDefault=new Cursor(Cursor.DEFAULT_CURSOR);
    private final Cursor curcorMove=new Cursor(Cursor.MOVE_CURSOR);
    private final Cursor curcorHand=new Cursor(Cursor.HAND_CURSOR);
    
    JPopupMenu popupmenuEdit = new JPopupMenu("Edit");   
    
    int undo_redo_id=0;
    int undo_redo_count=0;
    
    RoadMap roadMap=new RoadMap();
    Vector<Vehicle> vecVhc=new Vector<Vehicle>();
    Vector<Vector<Light> > vecLights=new Vector<>();
    
    int[] lightGreenIds=new int[0];
    Vector<Vector2> vecCrosspoint=new Vector<>();
    int[] crossWaitMilliTime=new int[0];
    double[] crossCheckDis=new double[0];
    final int crossCheckMilliInterval=1000;
    
    final int moveMilliInterval=10;
    
    
    int cntPassedVhc=0;
    int cntChangedLight=0;
    double sumPath=0.0;
    double sumTime=0.0;
    
    int tnCreateVhc=0;
    MainProgram mainFrame;
    public CityPannel(MainProgram
            mainFrame){
        super();
        this.mainFrame=mainFrame;
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        JMenuItem menuItemDelete = new JMenuItem("Delete");  
//        popupmenuEdit.add(menuItemCut);
//        popupmenuEdit.add(menuItemCopy);
//        popupmenuEdit.add(menuItemPaste);
        popupmenuEdit.add(menuItemDelete);
        
        menuItemDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                deleteMenuClicked();
            }
        });
        saveTemp();
        
        new Timer(moveMilliInterval, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                timerEventMoveVehicle(0.001*moveMilliInterval);
            }
        }).start();
        new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                timerEventCreateVehicle();
            }
        }).start();
        new Timer(crossCheckMilliInterval, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                timerEventLightChange();
            }
        }).start();
        
        addMouseListener(new MouseAdapter() { 
            @Override
            public void mousePressed(MouseEvent e){
                mousePressedEvent(e,e.getPoint().x, e.getPoint().y);
            }
            @Override
            public void mouseReleased(MouseEvent e){
                mouseReleasedEvent(e,e.getPoint().x, e.getPoint().y);
            } 
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e){
                mouseMovedEvent(e,e.getPoint().x, e.getPoint().y);
            }
            @Override
            public void mouseDragged(MouseEvent e){
                mouseDraggedEvent(e,e.getPoint().x, e.getPoint().y);
            }
        });
    }
    
    public void setMode(ModeType mt){
        mode=mt;
        if(mode==ModeType.MT_EDIT){
            stopSimulate();
        }else if(mode==ModeType.MT_SINMULATE){
            roadMap.makeAll();
            vecLights.removeAllElements();
            vecCrosspoint.removeAllElements();
            roadMap.makeLights(vecLights, vecCrosspoint);
            lightGreenIds=new int[vecLights.size()];
            crossWaitMilliTime=new int[vecLights.size()];
            crossCheckDis=new double[vecLights.size()];
            for(int i=0;i<crossCheckDis.length;i++){
                for (Light light : vecLights.elementAt(i)) {
                    double dd=vecCrosspoint.elementAt(i).dis(light.position);
                    if(crossCheckDis[i]==0.0 || crossCheckDis[i]<dd)
                        crossCheckDis[i]=dd;
                }
                crossCheckDis[i]-=Constants.LIGHT_IMAGE_BREADTH;
                crossCheckDis[i]*=Math.cos(Math.PI/vecLights.elementAt(i).size());
            }
        }
    }
    public void setEditMode(EditType et){
        if(editMode==EditType.ET_ADD){
            roadMap.addPointEnd();
        }
        editMode=et;
    }    
    public void setZoom(int zoom){
        scale=0.01*zoom;
    }
    @Override
    protected void paintComponent(Graphics grphcs){
        super.paintComponent(grphcs);
        roadMap.draw(grphcs,scale,mode==ModeType.MT_EDIT);
        for (Vehicle vehicle : vecVhc) {
            vehicle.draw(grphcs, scale);
        }
        if(mode==ModeType.MT_SINMULATE){
            for (Vector<Light> vecLight : vecLights) {
                for (Light light : vecLight) {
                    light.draw(grphcs, scale);
                }
            }
        }
    }
    public void timerEventMoveVehicle(double tSecs){
        if(mainFrame.flagRun){
            Vector<Vehicle> vecRmVhc=new Vector<>();
            for (Vehicle vhc : vecVhc) {
                vhc.move(tSecs);
                if(vhc.isOutTartget()){
                    vecRmVhc.addElement(vhc);
                }else{
                    if(vhc.checkPosition(vecVhc, vecLights)){
                        if(vhc.checkLights(vecLights, tSecs,roadMap.roadBreadth)){
                            vhc.incSpeed();
                            continue;
                        }
                    }
                    vhc.move(-tSecs);
                    vhc.decSpeed();
                }
            }
            for (Vehicle vhc : vecRmVhc) {
                float avgSpeed=(float)Math.round(vhc.totalLenth/vhc.totalTime*1000)/1000;
                sumPath+=vhc.totalLenth;
                sumTime+=vhc.totalTime;
                cntPassedVhc++;
                vecVhc.removeElement(vhc);
            }
        }
        mainFrame.repaint();
    }
    private void timerEventCreateVehicle(){
        if(mainFrame.flagRun){
            tnCreateVhc++;
            if(vecVhc.size()==0 || tnCreateVhc>=mainFrame.getSpawn()){
                int vt=(int) (Math.random()*Constants.STR_VEHICLE_NAMES.length);
                double maxSpeed=30+Math.random()*10;
                Polyline pathLine=roadMap.makeRandomPath(vt);
                if(pathLine!=null){
                    if(vecVhc.size()<20){
                        Vehicle vhc= new Vehicle(vt, pathLine, maxSpeed);
                        if(vhc.isValid(vecVhc)){
                            vecVhc.addElement(vhc);
                            tnCreateVhc=0;
                        }
                    }
                }
            }
        }
    }
    private void timerEventLightChange(){
        for(int lId=0;lId<lightGreenIds.length;lId++){
            if(crossWaitMilliTime[lId]>0){
                crossWaitMilliTime[lId]-=crossCheckMilliInterval;
                if(crossWaitMilliTime[lId]<0)crossWaitMilliTime[lId]=0;
            }
            if(crossWaitMilliTime[lId]<=3000){
                Vector<Light> vecLight=vecLights.elementAt(lId);
                vecLight.elementAt(lightGreenIds[lId]).state=Constants.LT_RED;
                if(mainFrame.flagRun){
                    if(crossWaitMilliTime[lId]==0){
                        Vector2 v0=vecCrosspoint.elementAt(lId);
                        if(checkCross(v0, crossCheckDis[lId])){
                            int kkk=calcBusyIdInCross(vecLight);
                            if(kkk<0)kkk=(lightGreenIds[lId]+(int)(Math.random()*vecLight.size()-1)+1)%vecLight.size();
                            lightGreenIds[lId]=kkk;
                            vecLight.elementAt(kkk).state=Constants.LT_GREEN;
                            crossWaitMilliTime[lId]=10000;
                            cntChangedLight++;
                        }
                    }
                }
            }
        }
    }
    private boolean checkCross(Vector2 v0,double maxDis){
        for(Vehicle vhc: vecVhc){
            double dis=v0.dis(vhc.curPos);
            if(dis<maxDis){
                if(dis<maxDis/2 || vhc.curPos.addNew(vhc.curDir.multNew(vhc.speed*1.0)).dis(v0)<maxDis)
                    return false;
            }
        }
        return true;
    }
    private int calcBusyIdInCross(Vector<Light> vecLight){
        for(int j=0;j<vecLight.size();j++){
            Light light=vecLight.elementAt(j);
            for (Vehicle vehicle : vecVhc) {
                if(!vehicle.checkLight(light, 0.001*moveMilliInterval*2, roadMap.roadBreadth)){
                    return j;
                }
            }
        }        
        return -1;
    }
    private int resSelfLinePossible=0;
    
    private int resLineId=-1;
    private Vector2 resLinePointIX=new Vector2();
    
    private Vector<Point> resNearControlPointIIs=new Vector<>();
    
    private int resMoveLineId=-1;
    private Vector2 vPreMoved=null;
    
    private Vehicle vhcDeleting=null;
    
    private void setNoFindRes(){
        resSelfLinePossible=0;
        resLineId=-1;
        resNearControlPointIIs.removeAllElements();
        resMoveLineId=-1;
        setCursor(curcorDefault);
        vhcDeleting=null;
    }
    private void mousePressedEvent(MouseEvent e, int x, int y){
        Vector2 v=new Vector2((double)x/scale, (double)y/scale);
        if(mode==ModeType.MT_EDIT){
            if(editMode==EditType.ET_ADD){
                if(e.getButton()==MouseEvent.BUTTON1){
                    if(resSelfLinePossible!=0){
                        roadMap.addPointForInput(resSelfLinePossible);
                        if(resLineId>=0){
                            setCursor(curcorHand);
                            boolean res=roadMap.split(resLineId, (int)resLinePointIX.x, resLinePointIX.y);
                            if(res){
                                if(roadMap.addPointEnd())saveTemp();
                            }
                        }
                    }
                }else if(e.getButton()==MouseEvent.BUTTON3){
                    if(roadMap.addPointEnd())saveTemp();
                }
            }else if(editMode==EditType.ET_EDIT){
                if(e.getButton()==MouseEvent.BUTTON1){
                    if(resNearControlPointIIs.size()==0){
                        if(resLineId>=0){
                            int resLinePointId=(int)resLinePointIX.x+1;
                            roadMap.midLines.elementAt(resLineId).points.insertElementAt(v,resLinePointId);
                            resNearControlPointIIs.addElement(new Point(resLineId, resLinePointId));
                            setCursor(curcorMove);
                        }
                    }else if(resMoveLineId>=0){
                        vPreMoved=v;
                    }
                }else if(e.getButton()==MouseEvent.BUTTON3){
                    if(resMoveLineId>=0){
                        popupmenuEdit.show(this, x, y);
                    }
                }
            }
        }else if(mode==ModeType.MT_SINMULATE){
            if(e.getButton()==MouseEvent.BUTTON3){
                vhcDeleting=null;
                for(Vehicle vhc: vecVhc){
                    if(Polyline.isPtInPolygon(v, vhc.points)){
                        vhcDeleting=vhc;break;
                    }
                }
                if(vhcDeleting!=null){
                    popupmenuEdit.show(this, x, y);
                }
            }else if(e.getButton()==MouseEvent.BUTTON1){
                for(int i=0;i<vecLights.size();i++){
                    for(int j=0;j<vecLights.elementAt(i).size();j++){
                        Light light=vecLights.elementAt(i).elementAt(j);
                        if(light.position.dis(v)<Constants.LIGHT_IMAGE_BREADTH/2){
                            if(light.state==Constants.LT_RED){
                                vecLights.elementAt(i).elementAt(lightGreenIds[i]).state=Constants.LT_RED;
                                light.state=Constants.LT_GREEN;
                                lightGreenIds[i]=j;
                                crossWaitMilliTime[i]=10000;
                            }else if(light.state==Constants.LT_GREEN){
                                light.state=Constants.LT_RED;
                                crossWaitMilliTime[i]=3000;
                            }
                        }
                    }
                }
            }
        }
    }
    private void mouseReleasedEvent(MouseEvent e, int x, int y){
        if(mode==ModeType.MT_EDIT){
            if(editMode==EditType.ET_EDIT){
                roadMap.reducePoints();
                if( resSelfLinePossible!=0 ||
                    resLineId!=-1 ||
                    resNearControlPointIIs.size()>0 ||
                    resMoveLineId!=-1){
                    saveTemp();
                }
            }
        }
    }
    private void mouseDraggedEvent(MouseEvent e, int x, int y){
        Vector2 v=new Vector2((double)x/scale, (double)y/scale);
        if(mode==ModeType.MT_EDIT){
            if(editMode==EditType.ET_EDIT){
                if(resNearControlPointIIs.size()>0){
                    Vector2 vPre=new Vector2();
                    for (Point ctrlII : resNearControlPointIIs) {
                        vPre.add(roadMap.midLines.elementAt(ctrlII.x).points.elementAt(ctrlII.y));
                    }
                    vPre.mult(1.0/resNearControlPointIIs.size());
                    for (Point ctrlII : resNearControlPointIIs) {
                        roadMap.midLines.elementAt(ctrlII.x).points.elementAt(ctrlII.y).set(v);
                    }
                    roadMap.makeSide();
                    if(!roadMap.checkIntersect()){
                        for (Point ctrlII : resNearControlPointIIs) {
                            roadMap.midLines.elementAt(ctrlII.x).points.elementAt(ctrlII.y).set(vPre);
                        }
                        roadMap.makeSide();
                    }
                }else if(resMoveLineId>=0){
                    roadMap.moveRoad(resMoveLineId, v.subNew(vPreMoved));
                    if(roadMap.checkIntersect()){
                        vPreMoved=v;
                    }else{
                        roadMap.moveRoad(resMoveLineId, vPreMoved.subNew(v));
                    }
                }
            }
        }
    }
    private void mouseMovedEvent(MouseEvent e, int x, int y){
        Vector2 v=new Vector2((double)x/scale, (double)y/scale);
        if(mode==ModeType.MT_EDIT){
            if(editMode==EditType.ET_ADD){
                roadMap.setPrePoint(v);
                resSelfLinePossible=roadMap.possibleAddingPre();
                if(resSelfLinePossible==0){
                    setNoFindRes();
                }else{
                    roadMap.makeSide();
                    resLineId=-1;
                    if(roadMap.currentMidLine().pointCount()>1)
                        resLineId=roadMap.findNearLinePointIX(v, resLinePointIX);
                    if(resLineId>=0){
                        setCursor(curcorCross);
                    }else{
                        if(roadMap.checkIntersectPre()){
                            if(resSelfLinePossible==1){
                                setCursor(curcorCross);
                            }else if(resSelfLinePossible==2){
                                setCursor(curcorMove);
                            }
                        }else{
                            setNoFindRes();
                        }
                    }
                }
            }else if(editMode==EditType.ET_EDIT){
                resNearControlPointIIs.removeAllElements();
                roadMap.findNearControlPointIIs(v, resNearControlPointIIs);
                if(resNearControlPointIIs.size()>0){
                    setCursor(curcorMove);
                }else{
                    resLineId=roadMap.findNearLinePointIX(v, resLinePointIX);
                    if(resLineId>=0){
                        setCursor(curcorCross);
                    }else{
                        resMoveLineId=roadMap.findRoadIncluding(v);
                        if(resMoveLineId>=0){
                            setCursor(curcorHand);
                            vPreMoved=v;
                        }else{
                            setNoFindRes();
                        }
                    }
                }
            }
        }
    }
    private void deleteMenuClicked(){
        if(mode==ModeType.MT_EDIT){
            if(resMoveLineId>=0){
                roadMap.deleteRoad(resMoveLineId);
            }            
        }else if(mode==ModeType.MT_SINMULATE){
            if(vhcDeleting!=null)
                vecVhc.removeElement(vhcDeleting);
        }
    }
    public void createNew(){
        roadMap=new RoadMap();
    }
    public boolean save(File file){
        try {
            PrintWriter pw=new PrintWriter(file);
            pw.println("Begin_City");
            if(!roadMap.save(pw))return false;
            pw.println("End_City");
            pw.flush();
            pw.close();
            return true;
        } catch (Exception ex) {}
        return false;
    }
    public boolean load(File file){
        try{
            int flag=0;
            BufferedReader br=new BufferedReader(new FileReader(file));
            for(int repeat=0;repeat<1000;repeat++){
                String line=br.readLine();
                if(line==null)return false;
                line=line.trim();
                if("".equals(line))continue;
                if(flag==0){
                    if("Begin_City".equalsIgnoreCase(line))flag=1;
                }else if(flag==1){
                    if("Begin_RoadMap".equalsIgnoreCase(line)){
                        RoadMap map=new RoadMap();
                        if(!map.load(br))return false;
                        roadMap=map;
                    }else if("End_City".equalsIgnoreCase(line)){
                        br.close();
                        return true;
                    }else{
                        int k=line.indexOf("=");
                        if(k<0)continue;
                        String propName=line.substring(0,k).trim();
                        String propValue=line.substring(k+1).trim();
                    }
                }else if(flag==2){
                }
            }
        }catch(Exception ex){}
        return false;
    }
    private File getTempFile(int tempId){
        return new File(tempFile, "tsd"+tempId+".tmp");
    }
    private boolean saveTemp(){
        File file=getTempFile(undo_redo_id+1);
        if(save(file)){
            undo_redo_id++;
            undo_redo_count=undo_redo_id;
            return true;
        }
        return false;
    }
    public boolean undo(){
        while(undo_redo_id>1){
            File file=getTempFile(--undo_redo_id);
            if(load(file))return true;
        }
        return false;
    }
    public boolean redo(){
        while(undo_redo_id<undo_redo_count){
            File file=getTempFile(++undo_redo_id);
            if(load(file))return true;
        }
        return false;        
    }
    
    void stopSimulate(){
        cntPassedVhc=0;
        cntChangedLight=0;
        sumPath=0.0;
        sumTime=0.0;
        vecVhc.removeAllElements();
    }
}
