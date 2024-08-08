package com.aeongames.stegsolveplus.ui.Ribbon;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.ComponentOrientation;
import java.awt.Dimension;

import org.pushingpixels.radiance.common.api.RadianceCommonCortex;
import org.pushingpixels.radiance.common.api.icon.RadianceIcon;
import org.pushingpixels.radiance.component.api.common.CommandAction;
import org.pushingpixels.radiance.component.api.common.CommandButtonPresentationState;
import org.pushingpixels.radiance.component.api.common.RichTooltip;
import org.pushingpixels.radiance.component.api.common.icon.EmptyRadianceIcon;
import org.pushingpixels.radiance.component.api.common.model.BaseCommandButtonPresentationModel.Overlay;
import org.pushingpixels.radiance.component.api.common.model.Command;
import org.pushingpixels.radiance.component.api.common.model.CommandButtonPresentationModel;
import org.pushingpixels.radiance.component.api.common.model.CommandGroup;
import org.pushingpixels.radiance.component.api.common.model.CommandMenuContentModel;
import org.pushingpixels.radiance.component.api.common.model.RichTooltipPresentationModel;
import org.pushingpixels.radiance.component.api.ribbon.JRibbonFrame;
import org.pushingpixels.radiance.component.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.radiance.component.api.ribbon.model.RibbonApplicationMenuCommand;
import org.pushingpixels.radiance.component.api.ribbon.projection.RibbonApplicationMenuCommandButtonProjection;


/**
 * there are lessons to get from creating a ribbon. 
 * the code is to extensive for some aspects that could be simpler
 * 
 *  for example. in general terms building the UI this way is more complex that it is necessary,
 *  it requires too much code and setup (it is not plug and play at least the ribbon)
 *  some of the functions are not the same as they are for Swing components (for example: 
 *  setActionEnabled(boolean) could be simplified with setEnabled, this way would match what you would come to expect from swing (UI) components.
 *   
 *  Radiance cannot be used with the WYSIWYG builders such as WindowBuilder on Eclipse or Netbeans UI builder. as it requires the LAF to match radiance. and some of the constructors will not play well 
 *  in a more general sense: 
 * 
 * 
 * when compared to using javaFX that separates the UI in more MVC way 
 * where the UI is the presentation layer and the Code are controllers
 * and where the UI is easier for graphic designers and UI designers to build via XML(esque) style 
 * in the same fashion XAMl works on C#. C++ & windows 
 * modern apps nowadays try and foster this separation. because works, and also facilitates the code readability. 
 * 
 * now if we compare to SWING historic way to write apps. yes using Radiance is nice. but IMO i think that this approach is not ideal nor friendly. I understand why it exist. 
 * and I appreciate the ton of works put into it. but i do think this library need to be simplified more. and also to be easier for designers to use 
 * the way it is code. is approachable for developers. but would be ideal if it were for designers that are not that Code inclined. 
 * 
 * and there is where for example React, and Atom and other JS based clients have the upper hand. because they are too ez for designers and ppl that are more inclined into the graphs and draws rather than code 
 * */

public abstract class ApplicationMenu extends JRibbonFrame {
	private static final long serialVersionUID = 3420896096883986633L;
	private ResourceBundle MenuTexts = ResourceBundle
            .getBundle("com.aeongames.stegsolveplus.ui.menu", Locale.getDefault());
	private Command OpenCommand,SaveCommand;
	
	public ApplicationMenu() {
		//create the basic component and its content.lets presume a "frame" 
		super();
		//fill the ribbon main menu with the nice options we desire 
		configureApplicationMenu();
		//set orientation and the accelerators
		applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
		KeyStroke keyStroke = (RadianceCommonCortex.getPlatform() == RadianceCommonCortex
                .Platform.MACOS) ? KeyStroke.getKeyStroke("meta alt E") :
                KeyStroke.getKeyStroke("alt shift E");
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        .put(keyStroke, "installTracingRepaintManager");
	}

	private void configureApplicationMenu() {
        Map <Command, CommandButtonPresentationModel.Overlay> applicationMenuOverlays = new HashMap<>();
        Map <Command, CommandButtonPresentationState> applicationMenuSecondaryStates = new HashMap<>();

        // "Open" primary
        OpenCommand = createCommandWithCotentHistory(applicationMenuOverlays,applicationMenuSecondaryStates,
        		commandActionEvent -> System.out.println("Invoked opening document"));

        // "Save" primary
        SaveCommand = createCommand(
        		applicationMenuOverlays,
        		applicationMenuSecondaryStates,
        		EmptyRadianceIcon.factory(),
        		MenuTexts.getString("AppMenuSave.text"),
        		commandActionEvent -> System.out.println("Invoked saving document"),
        		"S",
        		null);
        SaveCommand.setActionEnabled(false);
        //exit 
        Command amEntryExit = createCommand(
        		applicationMenuOverlays,
        		applicationMenuSecondaryStates,
        		EmptyRadianceIcon.factory(),
        		MenuTexts.getString("AppMenuExit.text"),
        		commandActionEvent -> System.exit(0),
        		"X",
        		null);
        //setting option
        var settings = Command.builder()
                .setText(MenuTexts.getString("AppMenuOptions.text"))
                .setIconFactory(EmptyRadianceIcon.factory())
                .setAction(commandActionEvent -> System.out.println("Invoked Options"))
                .build();
        applicationMenuOverlays.put(settings,
                CommandButtonPresentationModel.overlay().setActionKeyTip("T"));

        
        //add the menu items Into the actual container (Ribbon) 
        RibbonApplicationMenu applicationMenu = new RibbonApplicationMenu(
                new CommandGroup(OpenCommand, SaveCommand),
                new CommandGroup(amEntryExit)
                );
      

        applicationMenu.addFooterCommand(settings);

        try {

        	var bufferedIcon = new EmptyRadianceIcon(new Dimension(20,20));
        			/*
        			new BufferedImage_RadianceIcon(this.getClass().getResource(
                    "/com/aeongames/stegsolveplus/ui/appmenubutton-tooltip-main.png"));*/
            final float appMenuButtonTooltipImageRatio = (float)  bufferedIcon.getIconWidth()
                    / (float) bufferedIcon.getIconHeight();
            final int appMenuButtonTooltipImageInitialWidth = 100;
            final int appMenuButtonTooltipImageInitialHeight = (int)
                    (appMenuButtonTooltipImageInitialWidth
                            / appMenuButtonTooltipImageRatio);
            

            RibbonApplicationMenuCommandButtonProjection ribbonMenuCommandProjection =
                    new RibbonApplicationMenuCommandButtonProjection(
                            RibbonApplicationMenuCommand.builder()
                                    .setText(MenuTexts.getString("Ribbon.title"))
                                    .setSecondaryRichTooltip(RichTooltip.builder()
                                            .setTitle(MenuTexts.getString(
                                                    "Ribbon.tooltip.title"))
                                            .addDescriptionSection(MenuTexts.getString(
                                                    "Ribbon.tooltip.paragraph1"))
                                            .setMainIconFactory(() -> bufferedIcon)
                                            .addFooterSection(MenuTexts.getString(
                                                    "Ribbon.tooltip.footer1"))
                                            .build())
                                    .setSecondaryContentModel(applicationMenu)
                                    .build(),
                            CommandButtonPresentationModel.builder()
                                    .setPopupKeyTip("F")
                                    .setPopupRichTooltipPresentationModel(
                                            RichTooltipPresentationModel.builder().
                                                    setMainIconSize(new Dimension(
                                                            appMenuButtonTooltipImageInitialWidth,
                                                            appMenuButtonTooltipImageInitialHeight))
                                                    .build()
                                    ).build());

            ribbonMenuCommandProjection.setCommandOverlays(applicationMenuOverlays);
            ribbonMenuCommandProjection.setSecondaryLevelCommandPresentationState(
                    applicationMenuSecondaryStates);

            this.getRibbon().setApplicationMenuCommand(ribbonMenuCommandProjection);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

	private Command createCommandWithCotentHistory(Map<Command, Overlay> applicationMenuOverlays,
			Map<Command, CommandButtonPresentationState> applicationMenuSecondaryStates,
			CommandAction OptionCallback
			) {

        //if implemented gather file history.
        CommandMenuContentModel historyOpenMenu= loadHistory();
          
        return createCommand(
        		applicationMenuOverlays,
        		applicationMenuSecondaryStates,
        		EmptyRadianceIcon.factory(),
        		MenuTexts.getString("AppMenuOpen.text"),
        		OptionCallback,
        		"O",
        		historyOpenMenu
        		);
	}
	
	private Command createCommand(Map<Command, Overlay> applicationMenuOverlays,
			Map<Command, CommandButtonPresentationState> applicationMenuSecondaryStates,
			RadianceIcon.Factory IconFactory ,String Command_text, CommandAction ActionCallback,
			String key_Tip, 
			CommandMenuContentModel optionalModel
			) {
          
        var amEntryOpenbuilder = getCommandBuilderFor(
        		IconFactory,
        		Command_text,
        		ActionCallback
        		);
        if(optionalModel!=null) {
        	amEntryOpenbuilder.setSecondaryContentModel(optionalModel);
        }        
        var Command = amEntryOpenbuilder.build();
        if(applicationMenuSecondaryStates!=null) {
        	applicationMenuSecondaryStates.put(Command,CommandButtonPresentationState.MEDIUM);
        }
        if(applicationMenuOverlays!=null) {
        applicationMenuOverlays.put(Command,
                CommandButtonPresentationModel.overlay()
                        .setTextClick(CommandButtonPresentationModel.TextClick.ACTION)
                        .setActionKeyTip(key_Tip));
        }
        return Command;
	}
	
	private Command.Builder getCommandBuilderFor(RadianceIcon.Factory IconFactory ,String Command_text, CommandAction ActionCallback) {
		var builder = Command.builder();
		builder.setText(Command_text);
		builder.setIconFactory(IconFactory);
		builder.setAction(ActionCallback);
		return builder;
	}

	private CommandMenuContentModel loadHistory() {
		/*
		 java.util.List<Command> historyCommands = new ArrayList<>();

		 for (int i = 0; i < 5; i++) {
            Command command = Command.builder()
                    .setText(mf.format(new Object[]{i}))
                    .setIconFactory(Text_html.factory())
                    .setAction(commandActionEvent ->
                            System.out.println("Opening " + commandActionEvent.getCommand().getText()))
                    .build();
            historyCommands.add(command);
        } 
        
        
	        if(historyCommands!=null && historyCommands.size()>0) {
	        	CommandMenuContentModel historyOpenMenu = new CommandMenuContentModel(
	                    new CommandGroup(MenuTexts.getString("AppMenuOpen.secondary.textGroupTitle1"),
	                            historyCommands));
	        }  
		  
		 */
		return null;
	}

	
	
	
}
