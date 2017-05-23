package org.artoolkit.ar.samples.ARSimpleInteraction;

/**
 * Created by 4m1g0 on 27/04/17.
 */

public interface InfoDisplay {
    void setInformation(String text);
    void runOnUiThread(Runnable r);
}
