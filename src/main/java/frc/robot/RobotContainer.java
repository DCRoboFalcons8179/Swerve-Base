package frc.robot;

import com.pathplanner.lib.*;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.POVButton;
import frc.robot.autos.*;
import frc.robot.commands.*;
import frc.robot.subsystems.*;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
    /* Controllers */
    private final Joystick driver = new Joystick(0);

    /* Drive Controls */
    private final int testPathIndex = XboxController.Axis.kLeftX.value;
    private final int translationAxis = XboxController.Axis.kLeftY.value;
    private final int strafeAxis = XboxController.Axis.kLeftX.value;
    private final int rotationAxis = XboxController.Axis.kRightX.value;

    /* Driver Buttons */
    private final JoystickButton testPath = new JoystickButton(driver, XboxController.Button.kX.value);
    private final JoystickButton zeroGyro = new JoystickButton(driver, XboxController.Button.kY.value);
    private final JoystickButton robotCentric = new JoystickButton(driver, XboxController.Button.kLeftBumper.value);
    private final JoystickButton dumpToLogger = new JoystickButton(driver, XboxController.Button.kStart.value);
    private final POVButton perfectForward = new POVButton(driver, 0);
    private final POVButton turn90Degrees = new POVButton(driver, 270);
    private final POVButton fancyDanyPath = new POVButton(driver, 180);
    /* Subsystems */
    private final Swerve s_Swerve = new Swerve();
    private final Logger logger = Logger.getInstance();
    
    // Pathplanner Paths
    PathConstraints constraints = new PathConstraints(1, 1);

    PathPlannerTrajectory strightTrajectory = PathPlanner.loadPath("Straight", constraints);  //loadPath("Straight");
    PathPlannerTrajectory turn90DegreesTrajectory = PathPlanner.loadPath("turn90Degrees", constraints);  //loadPath("Straight");
    PathPlannerTrajectory fancyDancyPathTrajectory = PathPlanner.loadPath("fancyDancyPath", constraints);  //loadPath("Straight");

    
    /** The container for the robot. Contains subsystems, OI devices, and commands. */
    public RobotContainer() {

        // Raw Controller Inputs

        // Controller Filtering and Modification
        robotCentric.debounce(0.04).onTrue(new toggleFieldCentric(s_Swerve));


        s_Swerve.setDefaultCommand(
            new TeleopSwerve(
                s_Swerve, 
                    ()-> -driver.getRawAxis(translationAxis),
                    ()-> -driver.getRawAxis(strafeAxis),
                    ()-> driver.getRawAxis(rotationAxis)
            )
        );

        // Configure the button bindings
        configureButtonBindings();
        buttonCommands();
        configureLogger();
    }

    /**
     * Use this method to define your button->command mappings. Buttons can be created by
     * instantiating a {@link GenericHID} or one of its subclasses ({@link
     * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
     * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
     */
    private void configureButtonBindings() {
        /* Driver Buttons */
        zeroGyro.onTrue(new InstantCommand(() -> s_Swerve.zeroGyro()));
        dumpToLogger.whileTrue(new InstantCommand(() -> logger.dump()).andThen(new WaitCommand(0.5)));
        perfectForward.whileTrue(new TeleopSwerve(s_Swerve, () -> 0.25, () -> 0, () -> 0));
    }

    private void buttonCommands() {
        testPath.debounce(0.04).whileTrue(new doPathTrajectory(s_Swerve, strightTrajectory));
        turn90Degrees.debounce(0.04).whileTrue(new doPathTrajectory(s_Swerve, turn90DegreesTrajectory));
        fancyDanyPath.debounce(0.04).whileTrue(new doPathTrajectory(s_Swerve, fancyDancyPathTrajectory));
    }

    private void configureLogger() {
        logger.addValue("Translation Axis", () -> driver.getRawAxis(translationAxis));
    }

    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */

    public Command getAutonomousCommand() {
        // An ExampleCommand will run in autonomous
        return new exampleAuto(s_Swerve);
    }
}
