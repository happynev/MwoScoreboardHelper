package at.happynev.mwoscoreboardhelper.tracer;

import net.sourceforge.tess4j.Tesseract1;

/**
 * Created by Nev on 15.01.2017.
 */
public enum OcrConfig {
    TIME,
    NUMERIC,
    UNIT,
    DEFAULT,
    MECHS,
    STATUS, TEAMS, MATCHRESULT;

    private final String alphaChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-0123456789:_";
    private final String alphaCharsCAPS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ()-0123456789";
    private final String digits = "0123456789";

    public void applyConfig(Tesseract1 tess) {
        switch (this) {
            case TIME:
                tess.setTessVariable("tessedit_char_whitelist", digits + ":");
                break;
            case NUMERIC:
                tess.setTessVariable("tessedit_char_whitelist", digits);
                break;
            case UNIT:
                tess.setTessVariable("tessedit_char_whitelist", alphaChars + "[]()'");
                tess.setTessVariable("chs_leading_punct", "[");
                tess.setTessVariable("chs_trailing_punct1", "]");
                break;
            case MECHS:
                tess.setTessVariable("tessedit_char_whitelist", alphaCharsCAPS + "()");
                break;
            case TEAMS:
                tess.setTessVariable("tessedit_char_whitelist", "YourEnemyTa");
                break;
            case MATCHRESULT:
                tess.setTessVariable("tessedit_char_whitelist", "VICTORYDEFEAT");
                break;
            case STATUS:
                tess.setTessVariable("tessedit_char_whitelist", "ALIVED");
                break;
            case DEFAULT:
                tess.setTessVariable("tessedit_char_whitelist", alphaChars);
                break;
            default:
                break;
        }
    }
}
