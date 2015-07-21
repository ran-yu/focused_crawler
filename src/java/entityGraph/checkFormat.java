package entityGraph;

import org.apache.commons.validator.routines.UrlValidator;

// for future filtering use
public class checkFormat {
	public static boolean isUri(String x)
	{
		UrlValidator urlValidator = new UrlValidator();
		return urlValidator.isValid(x);
	}
	public static boolean isKnownDomainEntity(String x){
		if(x.contains("/resource/") || x.contains("/wiki/")){
			String[] y = x.split("/");
			if(!y[y.length-1].contains(".")){
				return true;
			}
		}
		return false;
	}
	public static boolean isEntity(String x){
		String[] y = x.split("/");
		if(!y[y.length-1].contains("."))
			return true;
		return false;
	}
}