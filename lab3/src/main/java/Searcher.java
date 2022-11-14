import attraction.Attraction;
import attraction.AttractionProperty;
import com.google.gson.Gson;
import geocode.FoundPlaces;
import geocode.Place;
import weather.Weather;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class Searcher {
    private HttpClient httpClient;
    private KeyUtils keyUtils;
    private Gson gson;

    public Searcher() throws IOException {
        this.httpClient = HttpClient.newBuilder().build();
        this.keyUtils = new KeyUtils();
        this.gson = new Gson();
    }

    public CompletableFuture<FoundPlaces> makeGhRequest(String placeName){
        String GhRequestString = String.format("%s?q=%s&key=%s",
                Constants.GEOCODE_URL,
                placeName,
                keyUtils.getGraphHopperApiKey());

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(GhRequestString))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> gson.fromJson(json, FoundPlaces.class));
    }

    public CompletableFuture<Weather> makeOwRequest(Place place){
        String OwRequestString = String.format("%s?lat=%s&lon=%s&appid=%s&units=metric",
                Constants.WEATHER_URL,
                place.getLat(),
                place.getLng(),
                keyUtils.getOpenWeatherApiKey());

        HttpRequest owRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(OwRequestString))
                .build();

        return httpClient.sendAsync(owRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> gson.fromJson(json, Weather.class));
    }

    public CompletableFuture<Attraction[]> makeOtmRequest(Place place, int radius){
        String OtmRequestString = String.format("%s?radius=%s&lon=%s&lat=%s&format=json&apikey=%s",
                Constants.OTM_ATTRACTION_URL,
                radius,
                place.getLng(),
                place.getLat(),
                keyUtils.getOtmApiKey());

        HttpRequest otmRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(OtmRequestString))
                .build();

        return httpClient.sendAsync(otmRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::getAttractDesc);
    }


    public Attraction[] getAttractDesc(String content){
        Attraction[] attrs = gson.fromJson(content, Attraction[].class);

        for (Attraction attr : attrs) {
            String requestString = String.format("%s?apikey=%s", Constants.OTM_INFO_URL+attr.getXid(), keyUtils.getOtmApiKey());
            HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(requestString)).build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(json -> {
                        //Properties data = gson.fromJson(info, Properties.class)
                        attr.setInfo(gson.fromJson(json, AttractionProperty.class));
                    });
        }
        return attrs;
    }
}


