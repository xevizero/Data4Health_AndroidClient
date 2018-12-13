package frassonlancellottilodi.data4health.utils;

/**
 * This class contains the Endpoints that the app needs to reach on the server to interact with the REST API
 */
public class Endpoints {

    //public static final String WEBSERVICE_IP = "http://xevizero.pythonanywhere.com";
    //public static final String WEBSERVICE_PORT ="";
    public static final String WEBSERVICE_IP = "http://192.168.1.24";
    public static final String WEBSERVICE_PORT =":5000";
    public static final String WEBSERVICE_ROUTE = "/android/";
    public static final String WEBSERVICE_URL = WEBSERVICE_IP + WEBSERVICE_PORT + WEBSERVICE_ROUTE;
    public static final String WEBSITE_URL = WEBSERVICE_IP + WEBSERVICE_PORT;
    public static final String WEBSERVICE_URL_REGISTER = WEBSERVICE_URL + "register";
    public static final String WEBSERVICE_URL_LOGIN = WEBSERVICE_URL + "login";
    public static final String WEBSERVICE_URL_PROFILE = WEBSERVICE_URL + "profile";
    public static final String WEBSERVICE_URL_EXTERNAL_PROFILE = WEBSERVICE_URL + "external_profile";
    public static final String WEBSERVICE_URL_NOTIFICATIONS = WEBSERVICE_URL + "notifications";
    public static final String WEBSERVICE_URL_NOTIFICATIONS_CLEAR_ALL = WEBSERVICE_URL + "notifications_clear_all";
    public static final String WEBSERVICE_URL_NOTIFICATIONS_REQUEST_ANSWER = WEBSERVICE_URL + "notifications_request_answer";
    public static final String WEBSERVICE_URL_IMAGES = WEBSERVICE_URL + "uploads";
    public static final String WEBSERVICE_URL_SEARCH = WEBSERVICE_URL + "research";
    public static final String WEBSERVICE_URL_FRIEND_REQUEST = WEBSERVICE_URL + "friend_request";
    public static final String WEBSERVICE_URL_REMOVE_FRIEND_REQUEST = WEBSERVICE_URL + "remove_friend_request";
    public static final String WEBSERVICE_URL_SUBSCRIPTION_REQUEST = WEBSERVICE_URL + "subscription_request";
    public static final String WEBSERVICE_URL_HOMEPAGE = WEBSERVICE_URL + "homepage";
    public static final String WEBSERVICE_URL_SYNC_HEALTH_DATA = WEBSERVICE_URL + "sync_health_data";
    public static final String WEBSERVICE_URL_MANAGE_AUTOMATEDSOS = WEBSERVICE_URL + "manage_automatedsos";
    public static final String WEBSERVICE_URL_EMERGENCY_AUTOMATEDSOS = WEBSERVICE_URL + "emergency_automatedsos";
}
