package frassonlancellottilodi.data4health.utils;

public class Endpoints {
    public static final String WEBSERVICE_IP = "http://192.168.1.129";
    public static final String WEBSERVICE_PORT = ":5000";
    public static final String WEBSERVICE_ROUTE = "/android/";
    public static final String WEBSERVICE_URL = WEBSERVICE_IP + WEBSERVICE_PORT + WEBSERVICE_ROUTE;
    public static final String WEBSITE_URL = WEBSERVICE_IP + WEBSERVICE_PORT;
    public static final String WEBSERVICE_URL_REGISTER = WEBSERVICE_URL + "register";
    public static final String WEBSERVICE_URL_LOGIN = WEBSERVICE_URL + "login";
}
