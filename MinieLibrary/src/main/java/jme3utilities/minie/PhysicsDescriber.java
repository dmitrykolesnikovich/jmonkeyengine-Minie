/*
 Copyright (c) 2013-2020, Stephen Gold
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
package jme3utilities.minie;

import com.jme3.bullet.MultiBody;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.SoftBodyWorldInfo;
import com.jme3.bullet.animation.PhysicsLink;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.Box2dShape;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.ConeCollisionShape;
import com.jme3.bullet.collision.shapes.Convex2dShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.collision.shapes.HullCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.MultiSphere;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.collision.shapes.SimplexCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.joints.Anchor;
import com.jme3.bullet.joints.Constraint;
import com.jme3.bullet.joints.JointEnd;
import com.jme3.bullet.joints.New6Dof;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.joints.SixDofJoint;
import com.jme3.bullet.joints.SoftAngularJoint;
import com.jme3.bullet.joints.SoftLinearJoint;
import com.jme3.bullet.joints.SoftPhysicsJoint;
import com.jme3.bullet.joints.motors.MotorParam;
import com.jme3.bullet.joints.motors.RotationalLimitMotor;
import com.jme3.bullet.joints.motors.TranslationalLimitMotor;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.PhysicsSoftBody;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.bullet.objects.infos.Aero;
import com.jme3.bullet.objects.infos.ConfigFlag;
import com.jme3.bullet.objects.infos.Sbcp;
import com.jme3.bullet.objects.infos.SoftBodyConfig;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.Validate;
import jme3utilities.debug.Describer;
import jme3utilities.math.MyVector3f;

/**
 * Generate compact textual descriptions of Minie data structures for debugging
 * purposes.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class PhysicsDescriber extends Describer {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(PhysicsDescriber.class.getName());
    // *************************************************************************
    // new methods exposed

    /**
     * Generate a textual description for a CollisionShape.
     *
     * @param shape (not null, unaffected)
     * @return description (not null)
     */
    public String describe(CollisionShape shape) {
        Validate.nonNull(shape, "shape");

        StringBuilder result = new StringBuilder(80);

        String desc = MyShape.describeType(shape);
        result.append(desc);

        if (shape instanceof Box2dShape) {
            Vector3f he = ((Box2dShape) shape).getHalfExtents(null);
            desc = describeHalfExtents(he);
            result.append(desc);

        } else if (shape instanceof BoxCollisionShape) {
            Vector3f he = ((BoxCollisionShape) shape).getHalfExtents(null);
            desc = describeHalfExtents(he);
            result.append(desc);

        } else if (shape instanceof CapsuleCollisionShape) {
            CapsuleCollisionShape capsule = (CapsuleCollisionShape) shape;
            int axis = capsule.getAxis();
            desc = MyString.axisName(axis);
            result.append(desc);

            float height = capsule.getHeight();
            float radius = capsule.getRadius();
            desc = describeHeightAndRadius(height, radius);
            result.append(desc);

        } else if (shape instanceof CompoundCollisionShape) {
            CompoundCollisionShape compound = (CompoundCollisionShape) shape;
            int numChildren = compound.countChildren();
            desc = String.format("[%d]", numChildren);
            result.append(desc);

        } else if (shape instanceof ConeCollisionShape) {
            ConeCollisionShape cone = (ConeCollisionShape) shape;
            int axis = cone.getAxis();
            desc = MyString.axisName(axis);
            result.append(desc);

            float height = cone.getHeight();
            float radius = cone.getRadius();
            desc = describeHeightAndRadius(height, radius);
            result.append(desc);

        } else if (shape instanceof Convex2dShape) {
            CollisionShape child = ((Convex2dShape) shape).getBaseShape();
            desc = describe(child);
            result.append('[');
            result.append(desc);
            result.append(']');

        } else if (shape instanceof CylinderCollisionShape) {
            CylinderCollisionShape cylinder = (CylinderCollisionShape) shape;
            int axis = cylinder.getAxis();
            desc = MyString.axisName(axis);
            result.append(desc);

            Vector3f he = cylinder.getHalfExtents(null);
            desc = describeHalfExtents(he);
            result.append(desc);

        } else if (shape instanceof GImpactCollisionShape) {
            int numV = ((GImpactCollisionShape) shape).countMeshVertices();
            desc = String.format("[%d]", numV);
            result.append(desc);

        } else if (shape instanceof HeightfieldCollisionShape) {
            int numV = ((HeightfieldCollisionShape) shape).countMeshVertices();
            desc = String.format("[%d]", numV);
            result.append(desc);

        } else if (shape instanceof HullCollisionShape) {
            int numV = ((HullCollisionShape) shape).countHullVertices();
            desc = String.format("[%d]", numV);
            result.append(desc);

        } else if (shape instanceof MeshCollisionShape) {
            int numV = ((MeshCollisionShape) shape).countMeshVertices();
            desc = String.format("[%d]", numV);
            result.append(desc);

        } else if (shape instanceof MultiSphere) {
            MultiSphere multiSphere = (MultiSphere) shape;
            result.append(" r[");
            int numSpheres = multiSphere.countSpheres();
            for (int sphereIndex = 0; sphereIndex < numSpheres; ++sphereIndex) {
                if (sphereIndex > 0) {
                    result.append(' '); // TODO listSeparator
                }
                float radius = multiSphere.getRadius(sphereIndex);
                result.append(MyString.describe(radius));
            }
            result.append(']');

        } else if (shape instanceof PlaneCollisionShape) {
            Plane plane = ((PlaneCollisionShape) shape).getPlane();
            result.append(" normal[");
            Vector3f normal = plane.getNormal();
            result.append(MyVector3f.describe(normal));
            result.append("] constant=");
            float constant = plane.getConstant();
            result.append(MyString.describe(constant));

        } else if (shape instanceof SimplexCollisionShape) {
            int numV = ((SimplexCollisionShape) shape).countMeshVertices();
            desc = String.format("[%d]", numV);
            result.append(desc);

        } else if (shape instanceof SphereCollisionShape) {
            SphereCollisionShape sphere = (SphereCollisionShape) shape;
            result.append(" r=");
            float radius = sphere.getRadius();
            result.append(MyString.describe(radius));

        } else {
            result.append('?');
        }

        result.append(" marg=");
        float margin = shape.getMargin();
        result.append(MyString.describe(margin));

        return result.toString();
    }

    /**
     * Generate a brief textual description for the specified
     * PhysicsSoftBody.Material
     *
     * @param material the Material to describe (not null, unaffected)
     * @return description (not null, not empty)
     */
    public String describe(PhysicsSoftBody.Material material) {
        String result = String.format(
                "Material stiffness[ang=%s lin=%s vol=%s]",
                MyString.describe(material.angularStiffness()),
                MyString.describe(material.linearStiffness()),
                MyString.describe(material.volumeStiffness()));
        return result;
    }

    /**
     * Generate a brief textual description for a PhysicsJoint.
     *
     * @param joint (not null, unaffected)
     * @return description (not null, not empty)
     */
    public String describe(PhysicsJoint joint) {
        StringBuilder result = new StringBuilder(40);

        String type = joint.getClass().getSimpleName();
        if (type.endsWith("Joint")) {
            type = MyString.removeSuffix(type, "Joint");
        }
        result.append(type);

        if (!joint.isEnabled()) {
            result.append(" DISABLED");
        }

        return result.toString();
    }

    /**
     * Describe the specified RotationalLimitMotor.
     *
     * @param motor the motor to describe (not null, unaffected)
     * @return descriptive text (not null, not empty)
     */
    public String describe(RotationalLimitMotor motor) {
        StringBuilder result = new StringBuilder(80);

        if (motor.isEnableMotor()) {
            float angle = motor.getAngle();
            result.append(angle);

            float lo = motor.getLowerLimit();
            float hi = motor.getUpperLimit();
            if (hi < lo) {
                result.append(" unlimited");
            } else {
                result.append(" lo=");
                result.append(MyString.describe(lo));
                result.append(" hi=");
                result.append(MyString.describe(hi));
            }

            result.append(" tgtV=");
            float targetV = motor.getTargetVelocity();
            result.append(MyString.describe(targetV));

            result.append(" cfm=");
            float cfm = motor.getNormalCFM();
            result.append(MyString.describe(cfm));

            result.append(" damp=");
            float damping = motor.getDamping();
            result.append(MyString.describe(damping));

            result.append(" maxMF=");
            float maxMF = motor.getMaxMotorForce();
            result.append(MyString.describe(maxMF));

            if (hi >= lo) {
                result.append(" lim[cfm=");
                cfm = motor.getStopCFM();
                result.append(MyString.describe(cfm));

                result.append(" erp=");
                float erp = motor.getERP();
                result.append(MyString.describe(erp));

                result.append(" maxMF=");
                maxMF = motor.getMaxLimitForce();
                result.append(MyString.describe(maxMF));

                result.append(" rest=");
                float rest = motor.getRestitution();
                result.append(MyString.describe(rest));

                result.append(" soft=");
                float soft = motor.getLimitSoftness();
                result.append(MyString.describe(soft));
                result.append(']');
            }
        } else {
            result.append(" DISABLED");
        }

        return result.toString();
    }

    /**
     * Generate a brief textual description for the specified SoftBodyWorldInfo.
     *
     * @param info the info to describe (not null, unaffected)
     * @return description (not null, not empty)
     */
    public String describe(SoftBodyWorldInfo info) {
        StringBuilder result = new StringBuilder(40);

        result.append("SbwInfo grav[");
        Vector3f grav = info.copyGravity(null);
        String description = MyVector3f.describe(grav);
        result.append(description);

        result.append("] offset=");
        float offset = info.waterOffset();
        description = MyString.describe(offset);
        result.append(description);

        result.append(" norm[");
        Vector3f norm = info.copyWaterNormal(null);
        description = MyVector3f.describe(norm);
        result.append(description);

        result.append("] water=");
        float water = info.waterDensity();
        description = MyString.describe(water);
        result.append(description);

        result.append(" air=");
        float air = info.airDensity();
        description = MyString.describe(air);
        result.append(description);

        result.append(" maxDisp=");
        float maxDisp = info.maxDisplacement();
        description = MyString.describe(maxDisp);
        result.append(description);

        return result.toString();
    }

    /**
     * Describe the indexed axis of the specified TranslationalLimitMotor.
     *
     * @param motor the motor to describe (not null, unaffected)
     * @param axisIndex which axis: 0&rarr;X, 1&rarr;Y, 2&rarr;Z
     * @return descriptive text (not null, not empty)
     */
    public String describe(TranslationalLimitMotor motor, int axisIndex) {
        Validate.inRange(axisIndex, "index", PhysicsSpace.AXIS_X,
                PhysicsSpace.AXIS_Z);
        StringBuilder result = new StringBuilder(80);
        Vector3f tmpVector = new Vector3f();

        if (motor.isEnabled(axisIndex)) {
            float offset = motor.getOffset(tmpVector).get(axisIndex);
            result.append(offset);

            float lo = motor.getLowerLimit(tmpVector).get(axisIndex);
            float hi = motor.getUpperLimit(tmpVector).get(axisIndex);
            if (hi < lo) {
                result.append(" unlimited");
            } else {
                result.append(" lo=");
                result.append(MyString.describe(lo));
                result.append(" hi=");
                result.append(MyString.describe(hi));
            }

            result.append(" tgtV=");
            float targetV = motor.getTargetVelocity(tmpVector).get(axisIndex);
            result.append(MyString.describe(targetV));

            result.append(" cfm=");
            float cfm = motor.getNormalCFM(tmpVector).get(axisIndex);
            result.append(MyString.describe(cfm));

            result.append(" damp=");
            float damping = motor.getDamping();
            result.append(MyString.describe(damping));

            result.append(" maxMF=");
            float maxMF = motor.getMaxMotorForce(tmpVector).get(axisIndex);
            result.append(MyString.describe(maxMF));

            if (hi >= lo) {
                result.append(" lim[cfm=");
                cfm = motor.getStopCFM(tmpVector).get(axisIndex);
                result.append(MyString.describe(cfm));

                result.append(" erp=");
                float erp = motor.getERP(tmpVector).get(axisIndex);
                result.append(MyString.describe(erp));

                result.append(" rest=");
                float restit = motor.getRestitution();
                result.append(MyString.describe(restit));

                result.append(" soft=");
                float softness = motor.getLimitSoftness();
                result.append(MyString.describe(softness));
                result.append(']');
            }
        } else {
            result.append(" DISABLED");
        }

        return result.toString();
    }

    /**
     * Describe the specified VehicleWheel.
     *
     * @param wheel the wheel to describe (not null, unaffected)
     * @return descriptive text (not null, not empty)
     */
    public String describe(VehicleWheel wheel) {
        StringBuilder result = new StringBuilder(80);

        boolean isFront = wheel.isFrontWheel();
        if (isFront) {
            result.append("frnt");
        } else {
            result.append("rear");
        }

        result.append(" r=");
        float r = wheel.getRadius();
        result.append(MyString.describe(r));

        result.append(" loc[");
        Vector3f loc = wheel.getLocation(null);
        result.append(MyVector3f.describe(loc));

        result.append("] axleDir[");
        Vector3f axleDir = wheel.getAxle(null);
        result.append(MyVector3f.describe(axleDir));

        result.append("] suspDown[");
        Vector3f suspDown = wheel.getDirection(null);
        result.append(MyVector3f.describe(suspDown));

        result.append("] fSlip=");
        float fSlip = wheel.getFrictionSlip();
        result.append(MyString.describe(fSlip));

        result.append(" brake=");
        float brake = wheel.getBrake();
        result.append(MyString.describe(brake));

        result.append(" engF=");
        float engF = wheel.getEngineForce();
        result.append(MyString.describe(engF));

        result.append(" rollInf=");
        float rollInf = wheel.getRollInfluence();
        result.append(MyString.describe(rollInf));

        result.append(" steer=");
        float steer = wheel.getSteerAngle();
        result.append(MyString.describe(steer));

        return result.toString();
    }

    /**
     * Generate the first line of a textual description for the specified
     * SoftBodyConfig.
     *
     * @param config the config to describe (not null, unaffected)
     * @return description (not null, not empty)
     */
    public String describe1(SoftBodyConfig config) {
        StringBuilder result = new StringBuilder(120);

        result.append("Config aero=");
        Aero aeroModel = config.aerodynamics();
        String description = aeroModel.toString();
        result.append(description);

        result.append(" flags=");
        int collisionFlags = config.collisionFlags();
        description = ConfigFlag.describe(collisionFlags);
        result.append(description);

        description = String.format(" maxVolRatio=%s timeScale=%s velCorr=%s",
                MyString.describe(config.get(Sbcp.MaxVolumeRatio)),
                MyString.describe(config.get(Sbcp.TimeScale)),
                MyString.describe(config.get(Sbcp.VelocityCorrection)));
        result.append(description);

        description = String.format(
                "  coef[damp=%s drag=%s fric=%s lift=%s pose=%s pres=%s volCons=%s]",
                MyString.describe(config.get(Sbcp.Damping)),
                MyString.describe(config.get(Sbcp.Drag)),
                MyString.describe(config.get(Sbcp.DynamicFriction)),
                MyString.describe(config.get(Sbcp.Lift)),
                MyString.describe(config.get(Sbcp.PoseMatching)),
                MyString.describe(config.get(Sbcp.Pressure)),
                MyString.describe(config.get(Sbcp.VolumeConservation)));
        result.append(description);

        return result.toString();
    }

    /**
     * Generate the 2nd line of a brief textual description for the specified
     * SoftBodyConfig.
     *
     * @param config the config to describe (not null, unaffected)
     * @return description (not null, not empty)
     */
    public String describe2(SoftBodyConfig config) {
        StringBuilder result = new StringBuilder(120);

        String description = String.format(
                " hardness[a=%s clk=%s clr=%s cls=%s k=%s r=%s s=%s]",
                MyString.describe(config.get(Sbcp.AnchorHardness)),
                MyString.describe(config.get(Sbcp.ClusterKineticHardness)),
                MyString.describe(config.get(Sbcp.ClusterRigidHardness)),
                MyString.describe(config.get(Sbcp.ClusterSoftHardness)),
                MyString.describe(config.get(Sbcp.KineticHardness)),
                MyString.describe(config.get(Sbcp.RigidHardness)),
                MyString.describe(config.get(Sbcp.SoftHardness)));
        result.append(description);

        description = String.format("  impSplit[clk=%s clr=%s cls=%s]",
                MyString.describe(config.get(Sbcp.ClusterKineticSplit)),
                MyString.describe(config.get(Sbcp.ClusterRigidSplit)),
                MyString.describe(config.get(Sbcp.ClusterSoftSplit)));
        result.append(description);

        description = String.format("  iters[cl=%d drift=%d pos=%d vel=%d]",
                config.clusterIterations(),
                config.driftIterations(),
                config.positionIterations(),
                config.velocityIterations());
        result.append(description);

        return result.toString();
    }

    /**
     * Describe the specified Anchor in the context of a PhysicsSpace.
     *
     * @param anchor the Anchor to describe (not null, unaffected)
     * @return descriptive text (not null, not empty)
     */
    public String describeAnchorInSpace(Anchor anchor) {
        StringBuilder result = new StringBuilder(80);

        String desc = describe(anchor);
        result.append(desc);

        result.append(" a=");
        PhysicsSoftBody bodyA = anchor.getSoftBody();
        long aId = bodyA.getObjectId();
        result.append(Long.toHexString(aId));
        if (!bodyA.isInWorld()) {
            result.append("_NOT_IN_WORLD");
        }

        result.append(" [");
        int nodeIndex = anchor.nodeIndex();
        result.append(nodeIndex);
        result.append(']');

        result.append(" b=");
        PhysicsRigidBody bodyB = anchor.getRigidBody();
        long bId = bodyB.getObjectId();
        result.append(Long.toHexString(bId));
        if (!bodyB.isInWorld()) {
            result.append("_NOT_IN_WORLD");
        }

        result.append(" piv[");
        Vector3f pivot = anchor.copyPivot(null);
        result.append(MyVector3f.describe(pivot));
        result.append(']');

        result.append(" infl=");
        float influence = anchor.influence();
        result.append(MyString.describe(influence));

        return result.toString();
    }

    /**
     * Describe the angular components of the specified SixDofJoint.
     *
     * @param joint the joint to describe (not null, unaffected)
     * @return descriptive text (not null, not empty)
     */
    public String describeAngular(SixDofJoint joint) {
        StringBuilder result = new StringBuilder(80);

        result.append("angles[");
        Vector3f angles = joint.getAngles(new Vector3f());
        result.append(MyVector3f.describe(angles));

        result.append("] lo["); // TODO describe axis-by-axis
        Vector3f lower = joint.getAngularLowerLimit(new Vector3f());
        result.append(MyVector3f.describe(lower));

        result.append("] hi[");
        Vector3f upper = joint.getAngularUpperLimit(new Vector3f());
        result.append(MyVector3f.describe(upper));
        result.append(']');

        return result.toString();
    }

    /**
     * Describe the specified Constraint in the context of a PhysicsSpace.
     *
     * @param constraint the Constraint to describe (not null, unaffected)
     * @return descriptive text (not null, not empty)
     */
    public String describeConstraintInSpace(Constraint constraint) {
        StringBuilder result = new StringBuilder(80);

        String desc = describe(constraint);
        result.append(desc);

        boolean endsCollide = constraint.isCollisionBetweenLinkedBodies();
        if (endsCollide) {
            result.append(" collide");
        } else {
            result.append(" NOcollide");
        }

        int numIterations = constraint.getOverrideIterations();
        if (numIterations != -1) {
            result.append(" iters=");
            result.append(numIterations);
        }

        int numDyn = 0;
        PhysicsRigidBody bodyA = constraint.getBodyA();
        if (bodyA != null) {
            result.append(" a=");
            long aId = bodyA.getObjectId();
            result.append(Long.toHexString(aId));
            if (!bodyA.isInWorld()) {
                result.append("_NOT_IN_WORLD");
            }
            if (bodyA.isDynamic()) {
                ++numDyn;
            }
        }

        PhysicsRigidBody bodyB = constraint.getBodyB();
        if (bodyB != null) {
            result.append(" b=");
            long bId = bodyB.getObjectId();
            result.append(Long.toHexString(bId));
            if (!bodyB.isInWorld()) {
                result.append("_NOT_IN_WORLD");
            }
            if (bodyB.isDynamic()) {
                ++numDyn;
            }
        }

        if (numDyn == 0) {
            result.append(" NO_DYNAMIC_END");
        }

        if (constraint.isFeedback()) {
            float impulse = constraint.getAppliedImpulse();
            result.append(" impulse=");
            result.append(impulse);
        }

        float bit = constraint.getBreakingImpulseThreshold();
        if (bit != Float.MAX_VALUE) {
            result.append(" bit=");
            result.append(MyString.describe(bit));
        }

        return result.toString();
    }

    /**
     * Describe the indexed degree of freedom of the specified New6Dof
     * constraint.
     *
     * @param constraint the constraint to describe (not null, unaffected)
     * @param dofIndex which degree of freedom (0&rarr;X translation, 1&rarr;Y
     * translation, 2&rarr;Z translation, 3&rarr;X rotation, 4&rarr;Y rotation,
     * 5&rarr;Z rotation)
     * @return descriptive text (not null, not empty)
     */
    public String describeDof(New6Dof constraint, int dofIndex) {
        StringBuilder result = new StringBuilder(80);

        float lo = constraint.get(MotorParam.LowerLimit, dofIndex);
        float hi = constraint.get(MotorParam.UpperLimit, dofIndex);
        if (hi < lo) {
            result.append(" free");
        } else if (hi == lo) {
            result.append(" locked");
        } else {
            result.append(" lo=");
            result.append(MyString.describe(lo));
            result.append(" hi=");
            result.append(MyString.describe(hi));
        }

        result.append(" motor[");
        if (constraint.isMotorEnabled(dofIndex)) {
            if (constraint.isServoEnabled(dofIndex)) { // servo motor
                result.append("servo target=");
                float target = constraint.get(MotorParam.ServoTarget, dofIndex);
                result.append(MyString.describe(target));
                result.append(" ");
            }

            result.append("tgtV=");
            float tgtV = constraint.get(MotorParam.TargetVelocity, dofIndex);
            result.append(MyString.describe(tgtV));

            result.append(" cfm=");
            float cfm = constraint.get(MotorParam.MotorCfm, dofIndex);
            result.append(MyString.describe(cfm));

            result.append(" erp=");
            float erp = constraint.get(MotorParam.MotorErp, dofIndex);
            result.append(MyString.describe(erp));

            result.append(" maxF=");
            float maxF = constraint.get(MotorParam.MaxMotorForce, dofIndex);
            result.append(MyString.describe(maxF));

        } else {
            result.append("off");
        }
        result.append(']');

        if (hi >= lo) { // limits/stops are enabled
            result.append(" lim[bounce=");
            float bounce = constraint.get(MotorParam.Bounce, dofIndex);
            result.append(MyString.describe(bounce));

            result.append(" cfm=");
            float cfm = constraint.get(MotorParam.StopCfm, dofIndex);
            result.append(MyString.describe(cfm));

            result.append(" erp=");
            float erp = constraint.get(MotorParam.StopErp, dofIndex);
            result.append(MyString.describe(erp));

            result.append(']');
        }

        result.append(" spring[");
        if (constraint.isSpringEnabled(dofIndex)) {
            result.append("eq=");
            float eq = constraint.get(MotorParam.Equilibrium, dofIndex);
            result.append(MyString.describe(eq));

            result.append(" stif=");
            float stif = constraint.get(MotorParam.Stiffness, dofIndex);
            result.append(MyString.describe(stif));

            result.append(" damp=");
            float damp = constraint.get(MotorParam.Damping, dofIndex);
            result.append(MyString.describe(damp));
        } else {
            result.append("off");
        }
        result.append(']');

        return result.toString();
    }

    /**
     * Briefly describe the collision group and collide-with groups of the
     * specified MultiBody.
     *
     * @param multiBody the object to describe (not null, unaffected)
     * @return descriptive text (not null, may be empty)
     */
    String describeGroups(MultiBody multiBody) {
        StringBuilder result = new StringBuilder(40);

        int group = multiBody.collisionGroup();
        if (group != PhysicsCollisionObject.COLLISION_GROUP_01) {
            result.append(" group=0x");
            result.append(Integer.toString(group, 16));
        }

        int groupMask = multiBody.collideWithGroups();
        if (groupMask != PhysicsCollisionObject.COLLISION_GROUP_01) {
            result.append(" gMask=0x");
            result.append(Integer.toString(groupMask, 16));
        }

        return result.toString();
    }

    /**
     * Briefly describe the collision group and collide-with groups of the
     * specified collision object.
     *
     * @param pco the object to describe (not null, unaffected)
     * @return descriptive text (not null, may be empty)
     */
    public String describeGroups(PhysicsCollisionObject pco) {
        StringBuilder result = new StringBuilder(40);

        int group = pco.getCollisionGroup();
        if (group != PhysicsCollisionObject.COLLISION_GROUP_01) {
            result.append(" group=0x");
            result.append(Integer.toString(group, 16));
        }

        int groupMask = pco.getCollideWithGroups();
        if (groupMask != PhysicsCollisionObject.COLLISION_GROUP_01) {
            result.append(" gMask=0x");
            result.append(Integer.toString(groupMask, 16));
        }

        return result.toString();
    }

    /**
     * Describe the specified PhysicsJoint in the context of the specified body.
     *
     * @param joint the joint to describe (not null, unaffected)
     * @param body one end of the joint
     * @return descriptive text (not null, not empty)
     */
    public String describeJointInBody(PhysicsJoint joint, PhysicsBody body) {
        StringBuilder result = new StringBuilder(80);

        String desc = describe(joint);
        result.append(desc);

        PhysicsBody otherBody = null;
        Vector3f pivot = null;
        if (joint.getBody(JointEnd.A) == body) {
            PhysicsBody bodyB = joint.getBody(JointEnd.B);
            if (bodyB != null) {
                otherBody = bodyB;
            }
            if (joint instanceof Constraint) {
                Constraint constraint = (Constraint) joint;
                pivot = constraint.getPivotA(null);
            }
        } else {
            assert joint.getBody(JointEnd.B) == body;
            PhysicsBody bodyA = joint.getBody(JointEnd.A);
            if (bodyA != null) {
                otherBody = bodyA;
            }
            if (joint instanceof Constraint) {
                Constraint constraint = (Constraint) joint;
                pivot = constraint.getPivotB(null);
            }
        }

        if (otherBody == null) {
            result.append(" single-ended");
        } else {
            result.append(" to:");
            result.append(otherBody.toString());
        }

        if (pivot != null) {
            result.append(" piv[");
            result.append(MyVector3f.describe(pivot));
            result.append(']');
        } else if (joint instanceof SoftAngularJoint) {
            SoftAngularJoint saj = (SoftAngularJoint) joint;
            result.append(" axis[");
            Vector3f axis = saj.copyAxis(null);
            result.append(MyVector3f.describe(axis));
            result.append(']');
        } else if (joint instanceof SoftLinearJoint) {
            SoftLinearJoint slj = (SoftLinearJoint) joint;
            result.append(" loc[");
            Vector3f location = slj.copyLocation(null);
            result.append(MyVector3f.describe(location));
            result.append(']');
        }

        return result.toString();
    }

    /**
     * Describe the specified joint in the context of a PhysicsSpace.
     *
     * @param joint the joint to describe (not null, unaffected)
     * @return descriptive text (not null, not empty)
     */
    public String describeJointInSpace(PhysicsJoint joint) {
        String result;
        if (joint instanceof Anchor) {
            result = describeAnchorInSpace((Anchor) joint);
        } else if (joint instanceof Constraint) {
            result = describeConstraintInSpace((Constraint) joint);
        } else {
            SoftPhysicsJoint softJoint = (SoftPhysicsJoint) joint;
            result = describeSoftJointInSpace(softJoint);
        }

        return result;
    }

    /**
     * Describe the linear components of the specified SixDofJoint.
     *
     * @param joint the joint to describe (not null, unaffected)
     * @return descriptive text (not null, not empty)
     */
    public String describeLinear(SixDofJoint joint) {
        StringBuilder result = new StringBuilder(80);

        result.append("offset[");
        Vector3f offset = joint.getPivotOffset(new Vector3f());
        result.append(MyVector3f.describe(offset));

        result.append("] lo["); // TODO describe axis-by-axis
        Vector3f lower = joint.getLinearLowerLimit(new Vector3f());
        result.append(MyVector3f.describe(lower));

        result.append("] hi[");
        Vector3f upper = joint.getLinearUpperLimit(new Vector3f());
        result.append(MyVector3f.describe(upper));
        result.append(']');

        return result.toString();
    }

    /**
     * Describe the specified soft joint in the context of a PhysicsSpace.
     *
     * @param joint the soft joint to describe (not null, unaffected)
     * @return descriptive text (not null, not empty)
     */
    public String describeSoftJointInSpace(SoftPhysicsJoint joint) {
        StringBuilder result = new StringBuilder(80);

        String desc = describe(joint);
        result.append(desc);

        PhysicsSoftBody bodyA = joint.getSoftBodyA();
        result.append(" a=");
        long aId = bodyA.getObjectId();
        result.append(Long.toHexString(aId));
        if (!bodyA.isInWorld()) {
            result.append("_NOT_IN_WORLD");
        }

        result.append(" [");
        int clusterIndex = joint.clusterIndexA();
        result.append(clusterIndex);
        result.append(']');

        PhysicsBody bodyB = joint.getBody(JointEnd.B);
        result.append(" b=");
        result.append(bodyB.toString());
        if (!bodyB.isInWorld()) {
            result.append("_NOT_IN_WORLD");
        }

        if (joint.isSoftSoft()) {
            result.append(" [");
            clusterIndex = joint.clusterIndexB();
            result.append(clusterIndex);
            result.append(']');
        }

        result.append(" cfm=");
        float cfm = joint.getCFM();
        result.append(MyString.describe(cfm));

        result.append(" erp=");
        float erp = joint.getERP();
        result.append(MyString.describe(erp));

        result.append(" split=");
        float split = joint.getSplit();
        result.append(MyString.describe(split));

        return result.toString();
    }

    /**
     * Describe the user of a collision object.
     *
     * @param pco the collision object to describe (not null, unaffected)
     * @return a descriptive string (not null, may be empty)
     */
    public String describeUser(PhysicsCollisionObject pco) {
        Validate.nonNull(pco, "collision object");

        StringBuilder builder = new StringBuilder(32);
        Object user = pco.getUserObject();
        if (user != null) {
            builder.append(" user=");
            builder.append(user.getClass().getSimpleName());
            if (user instanceof Spatial) {
                Spatial spatial = (Spatial) user;
                String name = spatial.getName();
                String text = MyString.quote(name);
                builder.append(text);
            } else if (user instanceof PhysicsLink) {
                PhysicsLink link = (PhysicsLink) user;
                String name = link.boneName();
                String text = MyString.quote(name);
                builder.append(text);
            }
        }

        return builder.toString();
    }
    // *************************************************************************
    // Describer methods

    /**
     * Create a copy of this PhysicsDescriber.
     *
     * @return a new instance, equivalent to this one
     * @throws CloneNotSupportedException if the superclass isn't cloneable
     */
    @Override
    public PhysicsDescriber clone() throws CloneNotSupportedException {
        PhysicsDescriber clone = (PhysicsDescriber) super.clone();
        return clone;
    }

    /**
     * Generate a textual description of a scene-graph control.
     *
     * @param control (not null)
     * @return description (not null)
     */
    @Override
    protected String describe(Control control) {
        Validate.nonNull(control, "control");
        String result = MyControlP.describe(control);
        return result;
    }

    /**
     * Test whether a scene-graph control is enabled.
     *
     * @param control control to test (not null)
     * @return true if the control is enabled, otherwise false
     */
    @Override
    protected boolean isControlEnabled(Control control) {
        Validate.nonNull(control, "control");

        boolean result = !MyControlP.canDisable(control)
                || MyControlP.isEnabled(control);

        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Describe the specified half extents.
     *
     * @param he the half extent for each local axis (not null, unaffected)
     * @return a bracketed description (not null, not empty)
     */
    private String describeHalfExtents(Vector3f he) {
        String desc = MyVector3f.describe(he);
        String result = String.format(" he[%s]", desc);

        return result;
    }

    /**
     * Describe the specified height and radius.
     *
     * @param height the height of the shape
     * @param radius the radius of the shape
     * @return a bracketed description (not null, not empty)
     */
    private String describeHeightAndRadius(float height, float radius) {
        String hText = MyString.describe(height);
        String rText = MyString.describe(radius);
        String result = String.format(" h=%s r=%s", hText, rText);

        return result;
    }
}
