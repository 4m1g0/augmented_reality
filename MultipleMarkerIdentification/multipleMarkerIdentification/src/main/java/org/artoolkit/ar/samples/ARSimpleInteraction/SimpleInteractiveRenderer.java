/*
 *  SimpleInteractiveRenderer.java
 *  ARToolKit5
 *
 *  Disclaimer: IMPORTANT:  This Daqri software is supplied to you by Daqri
 *  LLC ("Daqri") in consideration of your agreement to the following
 *  terms, and your use, installation, modification or redistribution of
 *  this Daqri software constitutes acceptance of these terms.  If you do
 *  not agree with these terms, please do not use, install, modify or
 *  redistribute this Daqri software.
 *
 *  In consideration of your agreement to abide by the following terms, and
 *  subject to these terms, Daqri grants you a personal, non-exclusive
 *  license, under Daqri's copyrights in this original Daqri software (the
 *  "Daqri Software"), to use, reproduce, modify and redistribute the Daqri
 *  Software, with or without modifications, in source and/or binary forms;
 *  provided that if you redistribute the Daqri Software in its entirety and
 *  without modifications, you must retain this notice and the following
 *  text and disclaimers in all such redistributions of the Daqri Software.
 *  Neither the name, trademarks, service marks or logos of Daqri LLC may
 *  be used to endorse or promote products derived from the Daqri Software
 *  without specific prior written permission from Daqri.  Except as
 *  expressly stated in this notice, no other rights or licenses, express or
 *  implied, are granted by Daqri herein, including but not limited to any
 *  patent rights that may be infringed by your derivative works or by other
 *  works in which the Daqri Software may be incorporated.
 *
 *  The Daqri Software is provided by Daqri on an "AS IS" basis.  DAQRI
 *  MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 *  THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS
 *  FOR A PARTICULAR PURPOSE, REGARDING THE DAQRI SOFTWARE OR ITS USE AND
 *  OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.
 *
 *  IN NO EVENT SHALL DAQRI BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL
 *  OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION,
 *  MODIFICATION AND/OR DISTRIBUTION OF THE DAQRI SOFTWARE, HOWEVER CAUSED
 *  AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE),
 *  STRICT LIABILITY OR OTHERWISE, EVEN IF DAQRI HAS BEEN ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 *
 *  Copyright 2015 Daqri, LLC.
 *  Copyright 2011-2015 ARToolworks, Inc.
 *
 *  Author(s): Julian Looser, Philip Lamb
 *
 */

package org.artoolkit.ar.samples.ARSimpleInteraction;

import android.app.Activity;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.NativeInterface;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.artoolkit.ar.base.rendering.Cube;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

/**
 * A simple Renderer that adds a marker and draws a spinning cube on it. The spinning is toggled
 * in the {@link #click} method, which is called from the activity when the user taps the screen.
 */
public class SimpleInteractiveRenderer extends ARRenderer {

    private static final int NUMBER_OF_MARKERS = 10;
    private static int visibleId = -1;
    private final InfoDisplay infoDisplay;
    List<Integer> markerIds = new ArrayList<>();
    Map<Integer, String> markerInformation = new HashMap<>();

    private Cube cube = new Cube(40.0f, 0.0f, 0.0f, 20.0f);
    private float angle = 0.0f;
    private boolean spinning = false;

    public SimpleInteractiveRenderer(InfoDisplay infoDisplay) {
        this.infoDisplay = infoDisplay;
    }

    /**
     * By overriding {@link #configureARScene}, the markers and other settings can be configured
     * after the native library is initialised, but prior to the rendering actually starting.
     */
    @Override
    public boolean configureARScene() {

        NativeInterface.arwSetPatternDetectionMode(NativeInterface.AR_MATRIX_CODE_DETECTION);
        NativeInterface.arwSetMatrixCodeType(NativeInterface.AR_MATRIX_CODE_4x4_BCH_13_9_3);

        for (int i = 0; i < NUMBER_OF_MARKERS; i++){
            markerIds.add(ARToolKit.getInstance().addMarker(String.format(Locale.ROOT, "single_barcode;%d;40", i)));
        }

        getMarkerInformation();

        return true;
    }

    private void getMarkerInformation() {
        markerInformation.put(0, "Marcador: 0\n\tZona: 23\n\t\tPallet:5\n\t\tNave:1\n\r\tEstado: OK");
        markerInformation.put(1, "Marcador: 1\n\tZona: 4\n\t\tPallet:1\n\t\tNave:3\n\r\tEstado: BAD");
    }

    public void draw(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadMatrixf(ARToolKit.getInstance().getProjectionMatrix(), 0);

        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glFrontFace(GL10.GL_CW);

        gl.glMatrixMode(GL10.GL_MODELVIEW);

        int markers = 0;

        for (final int markerID : markerIds) {
            if (ARToolKit.getInstance().queryMarkerVisible(markerID)) {
                markers++;
                if (markerID != visibleId) {
                    visibleId = markerID;
                    infoDisplay.runOnUiThread(new Runnable() //run on ui thread
                    {
                        public void run()
                        {
                            infoDisplay.setInformation(markerInformation.get(markerID));
                        }
                    });
                }

                gl.glLoadMatrixf(ARToolKit.getInstance().queryMarkerTransformation(markerID), 0);

                gl.glPushMatrix();
                gl.glRotatef(angle, 0.0f, 0.0f, 1.0f);
                cube.draw(gl);
                gl.glPopMatrix();

                if (spinning) angle += 5.0f;
            }
        }

        if (markers == 0) {
            visibleId = -1;
            infoDisplay.runOnUiThread(new Runnable() //run on ui thread
            {
                public void run()
                {
                    infoDisplay.setInformation("");
                }
            });
        }
    }

}