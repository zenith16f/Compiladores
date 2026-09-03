package automaton;

import java.nio.file.FileAlreadyExistsException;
import java.util.Set;

public final class Simbolo {
    public static final char EPSILON = 'ε';
    // You can add more constants here

    private static final Set<Character> ESPECIALES = Set.of(EPSILON);

    // Constructors
    private Simbolo() {
    }

    // Methods
    public static boolean IsSpecial(char simbolo) {
        return ESPECIALES.contains(simbolo);
    }
}
