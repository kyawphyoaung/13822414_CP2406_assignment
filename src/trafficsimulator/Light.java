
package trafficsimulator;

import java.awt.Graphics;

public class Light {
    public Vector2 direct=new Vector2();
    public int state;// Red, Yellow, Green
    
    public Vector2 position=new Vector2();
    public Light(Vector2 direct,Vector2 pos, int state){
        set(direct, pos, state);
    }
    public void set(Vector2 direct,Vector2 pos, int state){
        this.direct.set(direct);
        this.position.set(pos);
        this.state=state;
    }
    public void draw(Graphics g, double scale){
        double radius=Constants.LIGHT_IMAGE_BREADTH/2;
        g.setColor(Constants.LIGHT_COLORS[state]);
        g.fillRoundRect((int)Math.round(scale*(position.x-radius)), 
                        (int)Math.round(scale*(position.y-radius)), 
                        (int)Math.round(scale*(radius+radius)), 
                        (int)Math.round(scale*(radius+radius)), 
                        (int)Math.round(scale*(radius*1.5)), 
                        (int)Math.round(scale*(radius*1.5)));
        Vector2 vD=direct.multNew(radius*2.5).addNew(position);
        g.drawLine((int)Math.round(scale*position.x),
                    (int)Math.round(scale*position.y), 
                    (int)Math.round(scale*vD.x), 
                    (int)Math.round(scale*vD.y));
    }
}
