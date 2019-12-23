/*
 Copyright (c) 2019, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3utilities.minie.test.mesh;

import com.jme3.math.FastMath;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;

/**
 * A static, triangle-mode mesh that renders a prism.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Prism extends Mesh {
    // *************************************************************************
    // constants and loggers

    /**
     * number of axes in a vector
     */
    final private static int numAxes = 3;
    /**
     * number of vertices per triangle
     */
    final private static int vpt = 3;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(Prism.class.getName());
    // *************************************************************************
    // constructors

    /**
     * No-argument constructor needed by SavableClassUtil.
     */
    protected Prism() {
    }

    /**
     * Instantiate a right prism with regular ends that lie parallel with the
     * X-Z plane.
     *
     * @param numSides the number of sides for each end (&ge;3)
     * @param radius the radius of each end (in mesh units, &gt;0)
     * @param height the height of the prism (in mesh units, &gt;0)
     */
    public Prism(int numSides, float radius, float height) {
        Validate.inRange(numSides, "number of sides", 3, Integer.MAX_VALUE);
        Validate.positive(radius, "radius");
        Validate.positive(radius, "height");

        int numEndTriangles = 2 * (numSides - 2);
        int numSideTriangles = 2 * numSides;
        int numTriangles = numEndTriangles + numSideTriangles;
        int numFloats = numTriangles * vpt * numAxes;
        FloatBuffer positionBuffer = BufferUtils.createFloatBuffer(numFloats);
        setBuffer(VertexBuffer.Type.Position, numAxes, positionBuffer);

        float y = height / 2f;
        float interiorAngle = FastMath.TWO_PI / numSides; // in radians

        for (int sideIndex = 0; sideIndex < numSides; ++sideIndex) {
            float theta1 = sideIndex * interiorAngle; // in radians
            float x1 = radius * FastMath.sin(theta1);
            float z1 = radius * FastMath.cos(theta1);

            int nextSideIndex = MyMath.modulo(sideIndex + 1, numSides);
            float theta2 = nextSideIndex * interiorAngle; // in radians
            float x2 = radius * FastMath.sin(theta2);
            float z2 = radius * FastMath.cos(theta2);

            positionBuffer.put(x2).put(y).put(z2);
            positionBuffer.put(x1).put(y).put(z1);
            positionBuffer.put(x2).put(-y).put(z2);

            positionBuffer.put(x1).put(y).put(z1);
            positionBuffer.put(x1).put(-y).put(z1);
            positionBuffer.put(x2).put(-y).put(z2);

            if (sideIndex < numSides - 2) {
                float theta3 = (numSides - 1) * interiorAngle;
                float x3 = radius * FastMath.sin(theta3);
                float z3 = radius * FastMath.cos(theta3);

                positionBuffer.put(x1).put(y).put(z1);
                positionBuffer.put(x2).put(y).put(z2);
                positionBuffer.put(x3).put(y).put(z3);

                positionBuffer.put(x3).put(-y).put(z3);
                positionBuffer.put(x2).put(-y).put(z2);
                positionBuffer.put(x1).put(-y).put(z1);
            }
        }
        positionBuffer.flip();
        assert positionBuffer.limit() == positionBuffer.capacity();

        StarSlice.generateNormals(this);

        updateBound();
        setStatic();
    }
}