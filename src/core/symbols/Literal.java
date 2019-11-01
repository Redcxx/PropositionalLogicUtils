package core.symbols;

import core.exceptions.InvalidSymbolException;
import core.trees.LitNode;
import core.trees.Node;

import java.util.HashMap;
import java.util.Objects;

public class Literal extends Symbol {

    private String rawLiteral;
    private boolean isNegated;
    private Boolean rawLiteralTruthValue, isRawTautology;
    private int hashcode;

    private final static HashMap<Integer, Literal> created_instances = new HashMap<>();

    private Literal() {
    } // empty private constructor

    @Override
    public Node toNode() {
        return new LitNode(this);
    }

    /**
     * The core literal constructor for string
     *
     * @param rawLiteral the literal in String type, must be valid, non-tautology, non-contradiction and striped
     * @param isNegated  if this literal is negated
     */
    private Literal(String rawLiteral, boolean isNegated) {

        this.rawLiteral = rawLiteral;

        this.isNegated = isNegated;

        if (rawLiteral.equals(Character.toString(TAUTOLOGY))) {
            this.isRawTautology = this.rawLiteralTruthValue = true;

        } else if (rawLiteral.equals(Character.toString(CONTRADICTION))) {
            this.isRawTautology = this.rawLiteralTruthValue = false;

        } else {
            this.isRawTautology = this.rawLiteralTruthValue = null;
        }

        hashcode = Objects.hashCode(this.rawLiteral);
    }

    /*
        FACTORY METHODS BELOW
     */


    /**
     * This method return a Literal Object if given string is a valid representation of literal
     * This method sanitize input string before passing to actually constructor
     * Complexity is O(n)
     *
     * Note:
     * The followings are different literal: ~P, P.
     * But the followings are the same: P, ((((P)))), ~~P, ~~(((~~P))).
     *
     * @param str the input literal string
     * @return Literal Object
     * @throws InvalidSymbolException if null/empty/negation only/un-closed bracket/invalid character
     */
    public static Literal newInstance(String str) throws InvalidSymbolException {
        // check null
        if (str == null) {
            throw new InvalidSymbolException("Given Literal is null");
        }

        // parse negations and brackets
        boolean isNegated = false;

        // convert to array first instead of charAt due to extra index check in charAt, small string toCharArray
        // should be faster
        char[] chars = str.toCharArray();

        // for looping, this should later point to start of raw literal
        int startPointer = 0;

        // used to check if ends with right bracket, this should later point to end of raw literal
        int endPointer = chars.length - 1;

        // loop to ensure multiple brackets/negation sign are checked and removed
        while (startPointer < chars.length) {
            if (chars[startPointer] == ' ') {  // ignore space
                startPointer++;
            }

            // if starts with negation then negate the negation sign and increase start pointer
            else if (chars[startPointer] == NEG) {
                isNegated = !isNegated;
                startPointer += 1;
            }

            // then we check if starts with left bracket

            // if starts with left bracket, we check if ends with right bracket and increase pointer
            else if (chars[startPointer] == LBRACKET) {

                // then we check if ends with right bracket

                // skip spaces first
                while (endPointer > startPointer && chars[endPointer] == ' ') endPointer--;

                // throw exception if not encountered right bracket
                if (chars[endPointer] != RBRACKET) {
                    throw new InvalidSymbolException(String.format("Bracket is not enclosed properly for Literal " +
                            "\"%s\"", str));
                }

                // remove brackets
                startPointer += 1;
                endPointer -= 1;

            } // if (startsWithBracket)

            // if we reach here that means starts neither with negation nor right bracket
            // so we can terminate the loop
            else break;
        } // while (startPointer < chars.length)

        // remove trailing space
        while (endPointer > 0 && chars[endPointer] == ' ') endPointer--;

        // check empty
        if (endPointer < startPointer) {
            throw new InvalidSymbolException(String.format("Raw form is blank for literal \"%s\"", str));
        }

        /*
         * raw literal will be between pointers here
         */

        // get the raw literal, before checks if it is valid
        // so that we can easily print it if not valid
        String rawLiteral = String.valueOf(chars, startPointer, endPointer - startPointer + 1);

        // if this literal has already created then just return the old instance
        int key = Objects.hashCode(rawLiteral) ^ Objects.hashCode(isNegated);
        if (created_instances.containsKey(key)) {
            return created_instances.get(key);
        }

        // now we are creating new instance,
        // check if the raw string characters are letters
        for (int i = startPointer; i <= endPointer; i++) {
            if (!Character.isLetter(chars[i])) {
                // exception if not letter
                throw new InvalidSymbolException(String.format("Raw form of literal \"%s\" must contains letters " +
                        "only, not \"%s\"", str, rawLiteral));
            }
        }

        // everything checked, return Literal instance
        Literal new_instance = new Literal(rawLiteral, isNegated);
        created_instances.put(key, new_instance);
        return new_instance;
    }

    /*
        GETTER METHODS BELOW
     */

    @Override
    public String getRaw() {
        return this.rawLiteral;
    }

    @Override
    public String getFull() {
        return isNegated ? NEG + getRaw() : getRaw();
    }

    public boolean getTruthValue() {
        if (this.rawLiteralTruthValue == null) {
            throw new IllegalStateException(String.format("Access truth value before assignment for literal: \"%s\"",
                    getFull()));
        }
        return this.isNegated != this.rawLiteralTruthValue;  // same as isNegated ? !value : value;
    }

    private Boolean getTruthValueOrNull() {
        if (this.rawLiteralTruthValue == null) return null;
        else return this.isNegated != this.rawLiteralTruthValue;
    }

    public void invertNegation() {
        this.isNegated = !this.isNegated;
    }

    public boolean isNegated() {
        return this.isNegated;
    }

    public boolean isTautology() {
        if (this.isRawTautology == null) return false;
        return this.isNegated ? !this.isRawTautology : this.isRawTautology;
    }

    public boolean isContradiction() {
        if (this.isRawTautology == null) return false;
        return this.isNegated ? this.isRawTautology : !this.isRawTautology;
    }

    public boolean isAssigned() {
        return this.rawLiteralTruthValue != null;
    }

    public String toString() {
        return String.format("Full literal: %s,%nRaw literal: %s,%nNegated: %s,%n" +
                        "Tautology: %s,%nContradiction: %s,%nAssigned raw value: %s,%nTruth value: %s",
                this.getFull(), this.getRaw(), this.isNegated(), this.isTautology(),
                this.isContradiction(), this.rawLiteralTruthValue, this.getTruthValueOrNull());
    }

    /*
        SETTER METHODS BELOW
     */


    // this will override previous value if it is not tautology or contradiction
    // else fails
    public void assign(boolean value) {
        if (this.isTautology() || this.isContradiction()) {
            throw new IllegalStateException(
                    "Assign value to " + (this.isTautology() ? "tautology" : "contradiction") + " literal");
        }
        this.rawLiteralTruthValue = value;

        int same_literal_with_diff_negation_hash = hashcode ^ Objects.hashCode(!isNegated);
        if (created_instances.containsKey(same_literal_with_diff_negation_hash)) {
            created_instances.get(same_literal_with_diff_negation_hash).rawLiteralTruthValue = value;
        }
    }

    // override previous raw literal value if it is not tautology/contradiction
    // else stays the same
    public void assignIfNotTF(boolean value) {
        if (!this.isTautology() && !this.isContradiction()) {
            this.rawLiteralTruthValue = value;
        }
    }


    /*
        COMPARATOR METHODS BELLOW
     */

    public boolean rawEquals(Literal other) {
        if (other == null) throw new InvalidSymbolException("Given other literal for rawEquals is null");
        else return this.rawLiteral.equals(other.rawLiteral);
    }

    public boolean fullEquals(Literal other) {
        if (other == null) throw new InvalidSymbolException("Given literal for rawEquals is null");
        else return this.getFull().equals(other.getFull());
    }

    public boolean equals(Literal other) {
        if (other == null) throw new InvalidSymbolException("Given literal for equals is null");
        else return this.rawLiteral.equals(other.rawLiteral)
                && this.isNegated == other.isNegated
                && this.getTruthValueOrNull() == other.getTruthValueOrNull();
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    public int compareTo(Object other) {
        if (other instanceof Literal) {
            return hashCode() - other.hashCode();
        } else {
            throw new InvalidSymbolException("Comparing non-Literal to Literal");
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Literal) {
            return hashCode() == other.hashCode();
        } else {
            throw new InvalidSymbolException("Comparing non-Literal to Literal");
        }
    }

}