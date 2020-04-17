package info.tritusk.anchor;

public enum AnchorType {

    STANDARD(false), PERSONAL(true), PASSIVE(true), ADMIN(false);

    public final boolean passive;

    AnchorType(boolean passive) {
        this.passive = passive;
    }
    
}