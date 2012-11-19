package ch.imbApp.cash_next_door.alert;

import android.app.AlertDialog;
import android.content.Context;

public class Alerts {

	public final static String ALERT_HORIZONTALE = "Bitte Ger‰t waagrecht halten";
	public final static String ALERT_NO_GPS = "Bitte Ger‰t waagrecht halten";
	
	private static AlertDialog alertDialog;
	private static AlertDialog.Builder alertDialogBuilder;
	
	public static void init(Context context) {
		alertDialogBuilder = new AlertDialog.Builder(
				context);

		alertDialogBuilder.setTitle("Uncool");
	}
	
	public static void showDialog(String message) {
 System.out.println("oki");
		if(Alerts.alertDialog == null ||!Alerts.alertDialog.isShowing()) {

			alertDialogBuilder.setMessage(message);
			System.out.println("doki");
			Alerts.alertDialog = alertDialogBuilder.create();
			System.out.println("woki");
			Alerts.alertDialog.show();
			System.out.println("h‰‰‰‰?");
		}
	}
	
	public static void hideDialog() {
		
		if(Alerts.alertDialog != null && Alerts.alertDialog.isShowing()) {
			Alerts.alertDialog.dismiss();
		}
	}
}
