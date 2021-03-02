// BV Ue2 WS20/21 Vorgabe
//
// Copyright (C) 2017 by Klaus Jung
// All rights reserved.
// Date: 2017-07-15

package bv_ws20;


public class GeometricTransform {

	public enum InterpolationType { 
		NEAREST("Nearest Neighbour"), 
		BILINEAR("Bilinear");
		
		private final String name;       
	    private InterpolationType(String s) { name = s; }
	    public String toString() { return this.name; }
	};
	
	public void perspective(RasterImage src, RasterImage dst, double angle, double perspectiveDistortion, InterpolationType interpolation) {
		switch(interpolation) {
		case NEAREST:
			perspectiveNearestNeighbour(src, dst, angle, perspectiveDistortion);
			break;
		case BILINEAR:
			perspectiveBilinear(src, dst, angle, perspectiveDistortion);
			break;
		default:
			break;	
		}
		
	}

	/**
	 * @param src source image
	 * @param dst destination Image
	 * @param angle rotation angle in degrees
	 * @param perspectiveDistortion amount of the perspective distortion 
	 */
	public void perspectiveNearestNeighbour(RasterImage src, RasterImage dst, double angle, double perspectiveDistortion) {
		// TODO: implement the geometric transformation using nearest neighbour image rendering
		
		// NOTE: angle contains the angle in degrees, whereas Math trigonometric functions need the angle in radians
		double rad = Math.toRadians(angle);

			for (int y=0; y<dst.height; y++) { // bei src.height läuft er nur übers alte bild
				for (int x=0; x<dst.width; x++) {
					int posOrig = y * dst.width + x;
					double s = perspectiveDistortion;

					//verschiebung(translation) in die mitte (formel= x = x + tx)
					int xT = x - (dst.width/2);
					int yT = y - (dst.height/2);
					//skalierung auf neue bildgröße mit x = sx * x
					// 		φ = rad
					//		x ́=(cos(φ) x) / (s sin(φ) x + 1)
					//double xS = (Math.cos(rad)*xT) / (s * Math.sin(rad) * xT +1);
					double xS = xT / (Math.cos(rad)-xT * s * Math.sin(rad));
					//		y ́= y/ (s sin(φ) x + 1)
					//double yS = yT / (s * Math.sin(rad) * xT + 1);
					double yS = yT * (s * Math.sin(rad) * xS + 1); // fühlt sich smoother an wenn man xS statt xT nimmt

					// cZtr = s * x sin(angle) +1
					//xS = x cos(angle) / cZtr
					//yS = y / cZtr

					double xnew = xS + (src.width/2);
					double ynew = yS + (src.height/2);

					int pos_new = (int)ynew * src.width + (int)xnew;
					//int pos_new = y * dst.width + x;

					if(xnew>=0 && xnew < src.width && ynew >=0 && ynew < src.height){
						dst.argb[posOrig] = src.argb[pos_new];
					}else{
						dst.argb[posOrig] = 0xFFFFFFFF;
					}
				}
			}
	}


	/**
	 * @param src source image
	 * @param dst destination Image
	 * @param angle rotation angle in degrees
	 * @param perspectiveDistortion amount of the perspective distortion 
	 */
	public void perspectiveBilinear(RasterImage src, RasterImage dst, double angle, double perspectiveDistortion) {
		// TODO: implement the geometric transformation using bilinear interpolation
		
		// NOTE: angle contains the angle in degrees, whereas Math trigonometric functions need the angle in radians
		double rad = Math.toRadians(angle);

		for (int y=0; y<dst.height; y++) { // bei src.height läuft er nur übers alte bild
			for (int x=0; x<dst.width; x++) {
				int posOrig = y * dst.width + x;
				double s = perspectiveDistortion;

				//verschiebung(translation) in die mitte (formel= x = x + tx)
				int xT = x - (dst.width/2);
				int yT = y - (dst.height/2);
				//skalierung auf neue bildgröße mit x = sx * x
				double xS = xT / (Math.cos(rad)-xT * s * Math.sin(rad));
				double yS = yT * (s * Math.sin(rad) * xS + 1); // fühlt sich smoother an wenn man xS statt xT nimmt

				double xnew = xS + (src.width/2);
				double ynew = yS + (src.height/2);

				int pos_new = (int)ynew * src.width + (int)xnew;

				if(xnew<src.width && xnew>=0 && ynew<src.height && ynew>=0){

					int xnewint = (int)Math.floor(xnew);
					int ynewint = (int)Math.floor(ynew);
					double h = (xnew - xnewint);
					double v = (ynew - ynewint);

					// Farbwerte auslesen
					int A, B, C, D;
					A = src.argb[pos_new];
					if(ynew==0) { A = 0xFFFFFFFF; }
					if(pos_new+1 < src.argb.length){
						B = src.argb[pos_new + 1];
					} else{ B = 0xFFFFFFFF; }
					if(pos_new+src.width< src.argb.length) {
						C = src.argb[pos_new + src.width];
					}else { C = 0xFFFFFFFF; }
					if(pos_new+src.width+1 < src.argb.length) {
						D = src.argb[pos_new + src.width + 1];
					}else { D = 0xFFFFFFFF; }

					// rgb extrahieren
					int rA = (A >> 16) & 0xff;
					int gA = (A >> 8) & 0xff;
					int bA = A & 0xff;
					int rB = (B >> 16) & 0xff;
					int gB = (B >> 8) & 0xff;
					int bB = B & 0xff;
					int rC = (C >> 16) & 0xff;
					int gC = (C >> 8) & 0xff;
					int bC = C & 0xff;
					int rD = (D >> 16) & 0xff;
					int gD = (D >> 8) & 0xff;
					int bD = D & 0xff;

					int rNew = (int)(rA*(1-h)*(1-v) + rB*h*(1-v) + rC*(1-h)*v + rD*h*v);
					int gNew = (int)(gA*(1-h)*(1-v) + gB*h*(1-v) + gC*(1-h)*v + gD*h*v);
					int bNew = (int)(bA*(1-h)*(1-v) + bB*h*(1-v) + bC*(1-h)*v + bD*h*v);

					// Werte korrigieren
//					if (rNew > 255) { rNew = 255; }
//					else if (rNew < 0) { rNew = 0; }
//					if (gNew > 255) { gNew = 255; }
//					else if (gNew < 0) { gNew = 0; }
//					if (bNew > 255) { bNew = 255; }
//					else if (bNew < 0) { bNew = 0; }

					dst.argb[posOrig] = (0xFF << 24) | (rNew << 16) | (gNew << 8) | bNew;
			} else {
				dst.argb[posOrig] = 0xFFFFFFFF;
			}
			}
		}
 	}
}
