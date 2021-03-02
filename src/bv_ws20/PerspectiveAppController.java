// BV Ue2 WS20/21 Vorgabe
//
// Copyright (C) 2017 by Klaus Jung
// All rights reserved.
// Date: 2017-07-15

package bv_ws20;

import java.io.File;

import bv_ws20.GeometricTransform.InterpolationType;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class PerspectiveAppController {
	
	private static final String initialFileName = "courtyard-512.jpg";
	private static File fileOpenPath = new File(".");
	private static final double angleStepSize = 5.0;	// used for angle increment and decrement
	private static final double scaleX = 1.05;
	private static final double scaleY = 1.35;
	
	private static final GeometricTransform transform = new GeometricTransform();
	private double angle;
	private double distortion;

    @FXML
    private Slider angleSlider;

    @FXML
    private Label angleLabel;

    @FXML
    private Slider distortionSlider;

    @FXML
    private Label distortionLabel;

    @FXML
    private ComboBox<GeometricTransform.InterpolationType> interpolationSelection;

    @FXML
    private ImageView originalImageView;

    @FXML
    private ImageView transformedImageView;

    @FXML
    private Label messageLabel;

    @FXML
    void openImage() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(fileOpenPath); 
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images (*.jpg, *.png, *.gif)", "*.jpeg", "*.jpg", "*.png", "*.gif"));
		File selectedFile = fileChooser.showOpenDialog(null);
		if(selectedFile != null) {
			fileOpenPath = selectedFile.getParentFile();
			RasterImage img = new RasterImage(selectedFile);
			img.setToView(originalImageView);
	    	processImages();
	    	messageLabel.getScene().getWindow().sizeToScene();;
		}
    }
	
    @FXML
    void interpolationChanged() {
    	processImages();
    }

    @FXML
    void angleChanged() {
    	angle = angleSlider.getValue();
    	angleLabel.setText(String.format("%.0f°", angle));
    	processImages();
    }
    
    @FXML
    void distortionChanged() {
    	distortion = distortionSlider.getValue();
    	distortionLabel.setText(String.format("%.4f", distortion));
    	processImages();
    }
    
    @FXML 
    void speedTest() {
		RasterImage origImg = new RasterImage(originalImageView); 
		// enlarge view to get some space for rotation
		RasterImage transformedImg = new RasterImage((int)(origImg.width * scaleX), (int)(origImg.height * scaleY));
		
		InterpolationType type = interpolationSelection.getValue();
		double distortion = 0.001;
		
		long startTime = System.currentTimeMillis();
		
		int cnt = 0;
		for(int angle = 0; angle < 360; angle += angleStepSize) {
			transform.perspective(origImg, transformedImg, angle, distortion, type);
			cnt++;
		}
		
		long time = System.currentTimeMillis() - startTime;
		messageLabel.setText("Speed Test: Calculated " + cnt + " perspectives in " + time + " ms");
    }
    
	@FXML
	public void initialize() {
		// set combo boxes items
		interpolationSelection.getItems().addAll(InterpolationType.values());
		interpolationSelection.setValue(InterpolationType.NEAREST);
		
		// initialize parameters
		angleChanged();
		distortionChanged();
		
		// load and process default image
		RasterImage img = new RasterImage(new File(initialFileName));
		img.setToView(originalImageView);
		processImages();
	}
	
	private void processImages() {
		if(originalImageView.getImage() == null)
			return; // no image: nothing to do
		
		long startTime = System.currentTimeMillis();
		
		RasterImage origImg = new RasterImage(originalImageView); 
		// enlarge view to get some space for rotation
		RasterImage transformedImg = new RasterImage((int)(origImg.width * scaleX), (int)(origImg.height * scaleY));
		
		transform.perspective(origImg, transformedImg, angle, distortion, interpolationSelection.getValue());
		
		transformedImg.setToView(transformedImageView);
		
	   	messageLabel.setText("Processing time: " + (System.currentTimeMillis() - startTime) + " ms");
	}
	

	



}
