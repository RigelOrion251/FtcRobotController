/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import static java.lang.Math.abs;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;


/*
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When a selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all linear OpModes contain.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */

public class Setter
{
    int position;
    Boolean resetCycleStart;
    int lastPosition;

    Setter(int _position, Boolean _resetCycleStart, int _lastPosition)
    {
        position = _position;
        resetCycleStart = _resetCycleStart;
        lastPosition = _lastPosition;
    };

    public void equalTo (Setter o)
    {
        o.set_Position(position);
        o.set_ResetCycleStart(resetCycleStart);
        o.set_LastPosition(lastPosition);
    };

    public int get_Position()
    {
        return position;
    };

    public Boolean get_ResetCycleStart()
    {
        return resetCycleStart;
    };

    public int get_LastPosition()
    {
        return lastPosition;
    };

    public void set_Position(int _position)
    {
        position = _position;
    };

    public void set_ResetCycleStart(Boolean _resetCycleStart)
    {
        resetCycleStart = _resetCycleStart;
    };

    public void set_LastPosition(int _lastPosition)
    {
        lastPosition = _lastPosition;
    };

}

@TeleOp(name="Viper Lift: Linear TeleOpMode", group="Linear OpMode")
public class Viper_Lift_Linear_TeleOp extends LinearOpMode {

    // Declare OpMode members.
    private final ElapsedTime runtime = new ElapsedTime();
    private int position = 0;
    private Setter resetSettings;
    private int last_position = 500;
    private Boolean reset_cycle_start = Boolean.TRUE;

    @Override
    public void runOpMode() {

        resetSettings = new Setter(position, Boolean.TRUE, last_position);
        telemetry.addData("Status", "Initialized CV");
        telemetry.update();

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).
        DcMotor lift = hardwareMap.get(DcMotor.class, "lift_motor");
        TouchSensor bottom = hardwareMap.get(TouchSensor.class, "bottom_touch");

        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // Pushing the left stick forward MUST make robot go forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips
        lift.setDirection(DcMotor.Direction.REVERSE);
        lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        lift.setTargetPosition(0);
        lift.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            // Get power level for the lift and telemetry
            double liftPower = -gamepad1.left_stick_y;

            if (gamepad1.dpad_down && gamepad1.y)
            {
                position = 3000;
                lift.setTargetPosition(position);
            }

            if (gamepad1.dpad_down && gamepad1.b)
            {
                position = 2000;
                lift.setTargetPosition(position);
            }

            if (gamepad1.dpad_down && gamepad1.a)
            {
                position = 1000;
                lift.setTargetPosition(position);
            }

            if (gamepad1.dpad_down && gamepad1.x)
            {
                position = 50;
                lift.setTargetPosition(position);
            }

            int top_stop = 3400;
            int bottom_stop = 50;
            resetSettings.set_Position(position);
            resetSettings.set_ResetCycleStart(reset_cycle_start);
            resetSettings.set_LastPosition(last_position);
            resetSettings.equalTo
            (
                motor_setPowerNHold
                (
                    lift,
                    liftPower,
                    top_stop,
                    bottom_stop,
                    bottom,
                    resetSettings
                )
            );

            // Show the elapsed game time and lift power and position.
            telemetry.addData("Status", "Run Time: " + runtime);
            RobotLog.d("%f, %f, %d, %d", runtime.milliseconds(), liftPower, position);
            telemetry.addData("Motors", "Power (%.2f)", liftPower);

            // Push telemetry to the Driver Station.
            telemetry.update();
        }
    }

    /**
     * Add Power or Hold a motor in a position.
     */
    private Setter motor_setPowerNHold
    (
        DcMotor motor,
        double power,
        int top_stop,
        int bottom_stop,
        TouchSensor bottom,
        Setter _resetSettings
    ) {

        int position = _resetSettings.get_Position();
        int last_position = _resetSettings.get_LastPosition();
        Boolean resetCycleStart = _resetSettings.get_ResetCycleStart();
        Setter returnSettings;


        // Check that the top or bottom stops have not been exceeded
        if
        (
            (position > top_stop)
            &&
            (power > 0.0)
        )
        {
            power = 0.0;
        }

        if
        (
            (position < bottom_stop)
            &&
            (power < 0.0)
            &&
            (resetCycleStart)
        )
        {
            Boolean Stalled = Boolean.FALSE;

            while (opModeIsActive() && (!bottom.isPressed()) && !Stalled) {
                power = -0.1;
                position = motor.getCurrentPosition();

                if (position >= last_position)
                {
                    power = 0.0;
                    Stalled = Boolean.TRUE;
                }

                last_position = position;
            }

            motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            if (Stalled)
            {
                motor.setTargetPosition(5);
                motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                sleep(500)
            }

            motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            position = motor.getCurrentPosition();
            motor.setTargetPosition(position);
            motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            last_position = top_stop;
            resetCycleStart = Boolean.FALSE
        }
        else
        {
            resetCycleStart = Boolean.TRUE
        }

        // Check the Hold Condition (power level < 0.1)
        if (abs(power) > .1) {
            // If Operator is commanding a change in the lift
            // position, disable the encoder hold operation,
            // move the lift, and track the new hold position.

            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            motor.setPower(power);
            position = motor.getCurrentPosition();
            motor.setTargetPosition(position);
        } else {
            // If Operator is not commanding a change in the
            // lift position, engage the encoder hold operation
            // with a strength of 0.2 power level
            motor.setPower(0.2);
            motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        }

        returnSettings = new Setter(position, resetCycleStart, last_position);

        return returnSettings;
    } // end method motor_PowerNHold()

}

