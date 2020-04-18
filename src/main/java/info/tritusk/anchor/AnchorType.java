package info.tritusk.anchor;

import java.util.HashMap;

public class AnchorType {

    public static final AnchorType STANDARD = new AnchorType("standard", false, false);
    public static final AnchorType PERSONAL = new AnchorType("personal", true, false);
    public static final AnchorType PASSIVE = new AnchorType("passive", true, false);
    public static final AnchorType ADMIN = new AnchorType("admin", false, true);

    private static final HashMap<String, AnchorType> LOOKUP = new HashMap<>();

    public final String name;
    public final boolean passive;
    public final boolean perpetual;

    public AnchorType(String name, boolean passive, boolean perpetual) {
        this.name = name;
        this.passive = passive;
        this.perpetual = perpetual;
        LOOKUP.put(name, this);
    }

    public static AnchorType find(String name) {
        return LOOKUP.getOrDefault(name, STANDARD);
    }
    
}