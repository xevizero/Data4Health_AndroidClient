package frassonlancellottilodi.data4health.utils;

public class Endpoints {
    //flask run --host=192.168.1.24


    public static final String WEBSERVICE_IP = "http://10.0.2.2";
    public static final String WEBSERVICE_PORT = ":5000";
    public static final String WEBSERVICE_ROUTE = "/android/";
    public static final String WEBSERVICE_URL = WEBSERVICE_IP + WEBSERVICE_PORT + WEBSERVICE_ROUTE;
    public static final String WEBSITE_URL = WEBSERVICE_IP + WEBSERVICE_PORT;
    public static final String WEBSERVICE_URL_REGISTER = WEBSERVICE_URL + "register";
    public static final String WEBSERVICE_URL_LOGIN = WEBSERVICE_URL + "login";
    public static final String WEBSERVICE_URL_PROFILE = WEBSERVICE_URL + "profile";
    public static final String WEBSERVICE_URL_EXTERNAL_PROFILE = WEBSERVICE_URL + "external_profile";
    public static final String WEBSERVICE_URL_NOTIFICATIONS = WEBSERVICE_URL + "notifications";
    public static final String WEBSERVICE_URL_IMAGES = WEBSERVICE_URL + "uploads";
    public static final String WEBSERVICE_URL_SEARCH = WEBSERVICE_URL + "research";
}
