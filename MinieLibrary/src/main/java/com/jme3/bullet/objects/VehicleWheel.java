/*
 * Copyright (c) 2009-2018 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.bullet.objects;

import com.jme3.bullet.objects.infos.VehicleTuning;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;
import java.io.IOException;
import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * Information about one wheel of a PhysicsVehicle.
 *
 * @author normenhansen
 */
public class VehicleWheel
        implements JmeCloneable, Savable {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(VehicleWheel.class.getName());
    // *************************************************************************
    // fields TODO re-order

    /**
     * unique identifier of the btRaycastVehicle
     */
    private long vehicleId = 0L;
    /**
     * 0-origin index among the vehicle's wheels (&ge;0)
     */
    private int wheelIndex = 0;
    /**
     * copy of wheel type: true&rarr;front (steering) wheel,
     * false&rarr;non-front wheel
     */
    private boolean isFront;
    /**
     * location where the suspension connects to the chassis (in chassis
     * coordinates)
     */
    private Vector3f location = new Vector3f();
    /**
     * suspension direction (in chassis coordinates, typically down/0,-1,0)
     */
    private Vector3f suspensionDirection = new Vector3f();
    /**
     * axis direction (in chassis coordinates, typically to the right/-1,0,0)
     */
    private Vector3f axisDirection = new Vector3f();
    /**
     * copy of tuning parameters
     */
    private VehicleTuning tuning = new VehicleTuning();
    /**
     * copy of roll-influence factor (0&rarr;no roll torque, 1&rarr;realistic
     * behavior, default=1)
     */
    private float rollInfluence = 1f;
    /**
     * copy of wheel radius (in physics-space units, &gt;0)
     */
    private float radius = 0.5f;
    /**
     * copy of rest length of the suspension (in physics-space units)
     */
    private float restLength = 1f;
    /**
     * wheel location in physics-space coordinates
     */
    private Vector3f wheelWorldLocation = new Vector3f();
    /**
     * wheel orientation in physics-space coordinates
     */
    private Quaternion wheelWorldRotation = new Quaternion();
    /**
     * associated spatial, or null if none
     */
    private Spatial wheelSpatial;
    /**
     * reusable rotation matrix
     */
    private Matrix3f tmp_Matrix = new Matrix3f();
    /**
     * temporary storage during calculations
     */
    private Quaternion tmp_inverseWorldRotation = new Quaternion();
    /**
     * true &rarr; physics coordinates match local transform, false &rarr;
     * physics coordinates match world transform
     */
    private boolean applyLocal = false;
    // *************************************************************************
    // constructors

    /**
     * No-argument constructor needed by SavableClassUtil. Do not invoke
     * directly!
     */
    public VehicleWheel() {
    }

    /**
     * Instantiate a wheel.
     *
     * @param spat the associated spatial, or null if none
     * @param location the location where the suspension connects to the chassis
     * (in chassis coordinates, not null, unaffected)
     * @param direction the suspension direction (in chassis coordinates, not
     * null, unaffected, typically down/0,-1,0)
     * @param axle the axis direction (in chassis coordinates, not null,
     * unaffected, typically right/-1,0,0)
     * @param restLength the rest length of the suspension (in physics-space
     * units)
     * @param radius the wheel's radius (in physics-space units, &ge;0)
     * @param frontWheel true&rarr;front (steering) wheel, false&rarr;non-front
     * wheel
     */
    public VehicleWheel(Spatial spat, Vector3f location, Vector3f direction,
            Vector3f axle, float restLength, float radius, boolean frontWheel) {
        Validate.positive(radius, "radius");

        wheelSpatial = spat;
        this.location.set(location);
        this.suspensionDirection.set(direction);
        this.axisDirection.set(axle);
        this.isFront = frontWheel;
        this.restLength = restLength;
        this.radius = radius;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Apply this wheel's physics location and orientation to its associated
     * spatial, if any.
     */
    public void applyWheelTransform() {
        if (wheelSpatial == null) {
            return;
        }
        Quaternion localRotationQuat = wheelSpatial.getLocalRotation();
        Vector3f localLocation = wheelSpatial.getLocalTranslation();
        if (!applyLocal && wheelSpatial.getParent() != null) {
            localLocation.set(wheelWorldLocation).subtractLocal(wheelSpatial.getParent().getWorldTranslation());
            localLocation.divideLocal(wheelSpatial.getParent().getWorldScale());
            tmp_inverseWorldRotation.set(wheelSpatial.getParent().getWorldRotation()).inverseLocal().multLocal(localLocation);

            localRotationQuat.set(wheelWorldRotation);
            tmp_inverseWorldRotation.set(wheelSpatial.getParent().getWorldRotation()).inverseLocal().mult(localRotationQuat, localRotationQuat);

            wheelSpatial.setLocalTranslation(localLocation);
            wheelSpatial.setLocalRotation(localRotationQuat);
        } else {
            wheelSpatial.setLocalTranslation(wheelWorldLocation);
            wheelSpatial.setLocalRotation(wheelWorldRotation);
        }
    }

    /**
     * Copy this wheel's axis direction.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a new direction vector (in chassis coordinates, either
     * storeResult or a new instance)
     */
    public Vector3f getAxle(Vector3f storeResult) {
        if (storeResult == null) {
            return axisDirection.clone();
        } else {
            return storeResult.set(axisDirection);
        }
    }

    /**
     * Copy the location where the wheel touches the ground.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector (in physics-space coordinates, either
     * storeResult or a new instance)
     */
    public Vector3f getCollisionLocation(Vector3f storeResult) {
        Vector3f result = (storeResult == null) ? new Vector3f() : storeResult;
        getCollisionLocation(vehicleId, wheelIndex, result);
        return result;
    }

    /**
     * Copy the normal where the wheel touches the ground.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a unit vector (in physics-space coordinates, either storeResult
     * or a new instance)
     */
    public Vector3f getCollisionNormal(Vector3f storeResult) {
        Vector3f result = (storeResult == null) ? new Vector3f() : storeResult;
        getCollisionNormal(vehicleId, wheelIndex, result);
        return result;
    }

    /**
     * Calculate how much this wheel has turned since the last physics step.
     *
     * @return the rotation angle (in radians)
     */
    public float getDeltaRotation() {
        return getDeltaRotation(vehicleId, wheelIndex);
    }

    /**
     * Copy this wheel's suspension direction.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a new direction vector (in chassis coordinates, either
     * storeResult or a new instance)
     */
    public Vector3f getDirection(Vector3f storeResult) {
        if (storeResult == null) {
            return suspensionDirection.clone();
        } else {
            return storeResult.set(suspensionDirection);
        }
    }

    /**
     * Read the friction between this wheel's tire and the ground.
     *
     * @return the coefficient of friction
     */
    public float getFrictionSlip() {
        return tuning.getFrictionSlip();
    }

    /**
     * Copy the location where the suspension connects to the chassis.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a new location vector (in chassis coordinates, either storeResult
     * or a new instance)
     */
    public Vector3f getLocation(Vector3f storeResult) {
        if (storeResult == null) {
            return location.clone();
        } else {
            return storeResult.set(location);
        }
    }

    /**
     * Read the maximum force exerted by this wheel's suspension.
     *
     * @return the maximum force
     */
    public float getMaxSuspensionForce() {
        return tuning.getMaxSuspensionForce();
    }

    /**
     * Read the travel distance for this wheel's suspension.
     *
     * @return the maximum travel distance (in centimeters)
     */
    public float getMaxSuspensionTravelCm() {
        return tuning.getMaxSuspensionTravelCm();
    }

    /**
     * Read the radius of this wheel.
     *
     * @return the radius (in physics-space units, &ge;0)
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Read the rest length of this wheel.
     *
     * @return the length
     */
    public float getRestLength() {
        return restLength;
    }

    /**
     * Read this wheel's roll influence.
     *
     * @return the roll-influence factor
     */
    public float getRollInfluence() {
        return rollInfluence;
    }

    /**
     * Calculate to what extent the wheel is skidding (for skid sounds/smoke
     * etc.)
     *
     * @return the relative amount of traction (0&rarr;wheel is sliding,
     * 1&rarr;wheel has full traction)
     */
    public float getSkidInfo() {
        return getSkidInfo(vehicleId, wheelIndex);
    }

    /**
     * Read the stiffness for this wheel's suspension.
     *
     * @return the stiffness constant
     */
    public float getSuspensionStiffness() {
        return tuning.getSuspensionStiffness();
    }

    /**
     * Read this wheel's damping when the suspension is compressed.
     *
     * @return the damping
     */
    public float getWheelsDampingCompression() {
        return tuning.getSuspensionCompression();
    }

    /**
     * Read this wheel's damping when the suspension is expanded.
     *
     * @return the damping
     */
    public float getWheelsDampingRelaxation() {
        return tuning.getSuspensionDamping();
    }

    /**
     * Access the spatial associated with this wheel.
     *
     * @return the pre-existing instance, or null
     */
    public Spatial getWheelSpatial() {
        return wheelSpatial;
    }

    /**
     * Copy this wheel's location to the specified vector.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector (in physics-space coordinates, either
     * storeResult or a new instance)
     */
    public Vector3f getWheelWorldLocation(Vector3f storeResult) {
        if (storeResult == null) {
            return wheelWorldLocation.clone();
        } else {
            return storeResult.set(wheelWorldLocation);
        }
    }

    /**
     * Copy this wheel's orientation to the specified quaternion.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a quaternion (in physics-space coordinates, either storeResult or
     * a new instance)
     */
    public Quaternion getWheelWorldRotation(Quaternion storeResult) {
        if (storeResult == null) {
            return wheelWorldRotation.clone();
        } else {
            return storeResult.set(wheelWorldRotation);
        }
    }

    /**
     * Test whether physics coordinates should match the local transform of the
     * Spatial.
     *
     * @return true if matching local transform, false if matching world
     * transform
     */
    public boolean isApplyLocal() {
        return applyLocal;
    }

    /**
     * Test whether this wheel is a front wheel.
     *
     * @return true if front wheel, otherwise false
     */
    public boolean isFrontWheel() {
        return isFront;
    }

    /**
     * Alter whether physics coordinates should match the local transform of the
     * Spatial.
     *
     * @param applyLocal true&rarr;match local transform, false&rarr;match world
     * transform (default=false)
     */
    public void setApplyLocal(boolean applyLocal) {
        this.applyLocal = applyLocal;
    }

    /**
     * Alter the friction between this wheel's tire and the ground.
     * <p>
     * Should be about 0.8 for realistic cars, but can increased for better
     * handling. Set large (10000.0) for kart racers.
     *
     * @param coeff the desired coefficient of friction (default=10.5)
     */
    public void setFrictionSlip(float coeff) {
        tuning.setFrictionSlip(coeff);
        applyInfo();
    }

    /**
     * Alter whether this wheel is a front (steering) wheel.
     *
     * @param frontWheel true&rarr;front wheel, false&rarr;non-front wheel
     */
    public void setFrontWheel(boolean frontWheel) {
        this.isFront = frontWheel;
        applyInfo();
    }

    /**
     * Alter the maximum force exerted by this wheel's suspension.
     * <p>
     * Increase this if your suspension cannot handle the weight of your
     * vehicle.
     *
     * @param maxForce the desired maximum force (default=6000)
     */
    public void setMaxSuspensionForce(float maxForce) {
        tuning.setMaxSuspensionForce(maxForce);
        applyInfo();
    }

    /**
     * Alter the travel distance for this wheel's suspension.
     *
     * @param travelCm the desired maximum travel distance (in centimetres,
     * default=500)
     */
    public void setMaxSuspensionTravelCm(float travelCm) {
        tuning.setMaxSuspensionTravelCm(travelCm);
        applyInfo();
    }

    /**
     * Alter the radius of this wheel.
     *
     * @param radius the desired radius (in physics-space units, &ge;0,
     * default=0.5)
     */
    public void setRadius(float radius) {
        this.radius = radius;
        applyInfo();
    }

    /**
     * Alter the rest length of the suspension of this wheel.
     *
     * @param restLength the desired length (default=1)
     */
    public void setRestLength(float restLength) {
        this.restLength = restLength;
        applyInfo();
    }

    /**
     * Alter this wheel's roll influence.
     * <p>
     * The roll-influence factor reduces (or magnifies) the torque contributed
     * by this wheel that tends to cause the vehicle to roll over. This is a bit
     * of a hack, but it's quite effective.
     * <p>
     * If the friction between the tires and the ground is too high, you may
     * reduce this factor to prevent the vehicle from rolling over. You should
     * also try lowering the vehicle's center of mass.
     *
     * @param rollInfluence the desired roll-influence factor (0&rarr;no roll
     * torque, 1&rarr;realistic behavior, default=1)
     */
    public void setRollInfluence(float rollInfluence) {
        this.rollInfluence = rollInfluence;
        applyInfo();
    }

    /**
     * Alter the stiffness of this wheel's suspension.
     *
     * @param stiffness the desired stiffness constant (10&rarr;off-road buggy,
     * 50&rarr;sports car, 200&rarr;Formula-1 race car, default=5.88)
     */
    public void setSuspensionStiffness(float stiffness) {
        tuning.setSuspensionStiffness(stiffness);
        applyInfo();
    }

    /**
     * Assign this wheel to a vehicle.
     *
     * @param vehicleId the ID of the btRaycastVehicle (not zero)
     * @param wheelIndex index among the vehicle's wheels (&ge;0)
     */
    public void setVehicleId(long vehicleId, int wheelIndex) {
        Validate.nonZero(vehicleId, "vehicle ID");
        Validate.nonNegative(wheelIndex, "wheel index");

        this.vehicleId = vehicleId;
        this.wheelIndex = wheelIndex;
        applyInfo();
    }

    /**
     * Alter this wheel's damping when the suspension is compressed.
     * <p>
     * Set to k * 2 * FastMath.sqrt(m_suspensionStiffness) where k is the
     * damping ratio:
     * <p>
     * k = 0.0 undamped and bouncy, k = 1.0 critical damping, k between 0.1 and
     * 0.3 are good values
     *
     * @param damping the desired damping (0&rarr;no damping, default=0.83)
     */
    public void setWheelsDampingCompression(float damping) {
        tuning.setSuspensionCompression(damping);
        applyInfo();
    }

    /**
     * Alter this wheel's damping when the suspension is expanded.
     * <p>
     * Set to k * 2 * FastMath.sqrt(m_suspensionStiffness) where k is the
     * damping ratio:
     * <p>
     * k = 0.0 undamped and bouncy, k = 1.0 critical damping, k between 0.1 and
     * 0.3 are good values
     *
     * @param wheelsDampingRelaxation the desired damping (default=0.88)
     */
    public void setWheelsDampingRelaxation(float wheelsDampingRelaxation) {
        tuning.setSuspensionDamping(wheelsDampingRelaxation);
        applyInfo();
    }

    /**
     * Alter which spatial is associated with this wheel.
     *
     * @param wheelSpatial the desired spatial, or null for none
     */
    public void setWheelSpatial(Spatial wheelSpatial) {
        this.wheelSpatial = wheelSpatial;
    }

    /**
     * Update this wheel's location and orientation.
     */
    public void updatePhysicsState() {
        getWheelLocation(vehicleId, wheelIndex, wheelWorldLocation);
        getWheelRotation(vehicleId, wheelIndex, tmp_Matrix);
        wheelWorldRotation.fromRotationMatrix(tmp_Matrix);
    }
    // *************************************************************************
    // JmeCloneable methods

    /**
     * Callback from {@link com.jme3.util.clone.Cloner} to convert this
     * shallow-cloned wheel into a deep-cloned one, using the specified cloner
     * and original to resolve copied fields.
     *
     * @param cloner the cloner that's cloning this wheel (not null)
     * @param original the instance from which this instance was shallow-cloned
     * (unused)
     */
    @Override
    public void cloneFields(Cloner cloner, Object original) {
        location = cloner.clone(location);
        suspensionDirection = cloner.clone(suspensionDirection);
        axisDirection = cloner.clone(axisDirection);
        tuning = cloner.clone(tuning);
        wheelWorldLocation = cloner.clone(wheelWorldLocation);
        wheelWorldRotation = cloner.clone(wheelWorldRotation);
        wheelSpatial = cloner.clone(wheelSpatial);
        tmp_Matrix = cloner.clone(tmp_Matrix);
        tmp_inverseWorldRotation = cloner.clone(tmp_inverseWorldRotation);
    }

    /**
     * Create a shallow clone for the JME cloner.
     *
     * @return a new instance
     */
    @Override
    public VehicleWheel jmeClone() {
        try {
            VehicleWheel clone = (VehicleWheel) super.clone();
            return clone;
        } catch (CloneNotSupportedException exception) {
            throw new RuntimeException(exception);
        }
    }
    // *************************************************************************
    // Savable methods

    /**
     * De-serialize this wheel, for example when loading from a J3O file.
     *
     * @param im importer (not null)
     * @throws IOException from importer
     */
    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
        wheelSpatial = (Spatial) capsule.readSavable("wheelSpatial", null);
        isFront = capsule.readBoolean("frontWheel", false);
        location = (Vector3f) capsule.readSavable("wheelLocation",
                new Vector3f());
        suspensionDirection = (Vector3f) capsule.readSavable("wheelDirection",
                new Vector3f());
        axisDirection = (Vector3f) capsule.readSavable("wheelAxle",
                new Vector3f());
        tuning = (VehicleTuning) capsule.readSavable("tuning",
                new VehicleTuning());
        rollInfluence = capsule.readFloat("rollInfluence", 1f);
        radius = capsule.readFloat("wheelRadius", 0.5f);
        restLength = capsule.readFloat("restLength", 1f);
    }

    /**
     * Serialize this wheel, for example when saving to a J3O file.
     *
     * @param ex exporter (not null)
     * @throws IOException from exporter
     */
    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(wheelSpatial, "wheelSpatial", null);
        capsule.write(isFront, "frontWheel", false);
        capsule.write(location, "wheelLocation", new Vector3f());
        capsule.write(suspensionDirection, "wheelDirection", new Vector3f());
        capsule.write(axisDirection, "wheelAxle", new Vector3f());
        capsule.write(tuning, "tuning", new VehicleTuning());
        capsule.write(rollInfluence, "rollInfluence", 1f);
        capsule.write(radius, "wheelRadius", 0.5f);
        capsule.write(restLength, "restLength", 1f);
    }
    // *************************************************************************
    // private methods

    private void applyInfo() {
        if (vehicleId != 0L) {
            applyInfo(vehicleId, wheelIndex,
                    getSuspensionStiffness(),
                    getWheelsDampingRelaxation(),
                    getWheelsDampingCompression(),
                    getFrictionSlip(),
                    rollInfluence,
                    getMaxSuspensionTravelCm(),
                    getMaxSuspensionForce(),
                    radius,
                    isFront,
                    restLength);
        }
    }

    native private void applyInfo(long wheelId, int wheelIndex,
            float suspensionStiffness,
            float wheelsDampingRelaxation,
            float wheelsDampingCompression,
            float frictionSlip,
            float rollInfluence,
            float maxSuspensionTravelCm,
            float maxSuspensionForce,
            float wheelsRadius,
            boolean frontWheel,
            float suspensionRestLength);

    native private void getCollisionLocation(long wheelId, int wheelIndex,
            Vector3f vec);

    native private void getCollisionNormal(long wheelId, int wheelIndex,
            Vector3f vec);

    native private float getDeltaRotation(long wheelId, int wheelIndex);

    native private float getSkidInfo(long wheelId, int wheelIndex);

    native private void getWheelLocation(long vehicleId, int wheelId,
            Vector3f location);

    native private void getWheelRotation(long vehicleId, int wheelId,
            Matrix3f location);
}
