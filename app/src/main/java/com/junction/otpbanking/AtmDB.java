package com.junction.otpbanking;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import com.google.gson.Gson;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public class AtmDB {
    public static AtmDB atmDB = new AtmDB();

    private Map<String, Atm> atmHashMap = new HashMap<>();

    @Setter
    @Getter
    private Location location;
    private Map<String, Atm> lastFilteredAtms;

    private AtmDB() {
//        fillData();
    }

    void fillData(Context context) {
        Map<String, Atm> atmMap = new HashMap<>();

        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.atm_csv_eng);
            CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1),
                    ';',
                    CSVParser.DEFAULT_QUOTE_CHARACTER, 1);

            List<String[]> records = csvReader.readAll();

            for (int i = 1; i < records.size() - 2; i += 3) {
                Atm atm = new Atm(
                        String.valueOf(i),
                        Double.parseDouble(records.get(i)[5].replace(",", ".")),
                        Double.parseDouble(records.get(i)[6].replace(",", ".")),
                        new int[48],
                        new Random().nextInt(10000),
                        records.get(i)[4],
                        "Y" .equals(records.get(i)[1]),
                        true,
                        Long.MAX_VALUE
                );

                int[] lineCounts = new int[48];
                for (int j = 0; j < lineCounts.length; j++) {
//                    lineCounts[j] = (Integer.parseInt(records.get(i)[j + 8]) +
//                            Integer.parseInt(records.get(i + 1)[j + 8]) +
//                            Integer.parseInt(records.get(i + 2)[j + 8]))
//                            / 3;
                    lineCounts[j] = new Random().nextInt(1000);
                }

                atm.setLineCount(lineCounts);
                atmMap.put(String.valueOf(i), atm);
            }

            this.atmHashMap = atmMap;

        } catch (IOException e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }
//
//        Atm atm0 = new Atm("0", 47.493460, 19.053233, new Random().nextInt(10), 1000, "address1", true, true, Long.MAX_VALUE);
//        atmHashMap.put("0", atm0);
//
//        atm0 = new Atm("1", 47.492546, 19.054832, new Random().nextInt(10), 1000, "address2", true, true, Long.MAX_VALUE);
//        atmHashMap.put("1", atm0);
//
//        atm0 = new Atm("2", 47.490937, 19.059745, new Random().nextInt(10), 1000, "address3", true, true, Long.MAX_VALUE);
//        atmHashMap.put("2", atm0);
//
//        atm0 = new Atm("3", 47.494352, 19.060657, new Random().nextInt(10), 1000, "address4", true, true, Long.MAX_VALUE);
//        atmHashMap.put("3", atm0);
//
//        atm0 = new Atm("4", 47.486815, 19.071414, 1000, 1000, "address5", true, true, Long.MAX_VALUE);
//        atmHashMap.put("4", atm0);
//
//        atm0 = new Atm("5", 47.488080, 19.063659, new Random().nextInt(10), 1000, "address6", true, true, Long.MAX_VALUE);
//        atmHashMap.put("5", atm0);
//
//        atm0 = new Atm("6", 47.498708, 19.057182, new Random().nextInt(10), 1000, "address7", true, true, Long.MAX_VALUE);
//        atmHashMap.put("6", atm0);
    }

    public Map<String, Atm> getAllAtms() {
        return this.atmHashMap;
    }

    public Map<String, Atm> getAtmsWithDirectionsApiTimingsPriority() {
        Map<String, Atm> prioritizedMap;

        Comparator<Map.Entry<String, Atm>> valueComparator = (atmEntry1, atmEntry2) -> {
            // count timings with including line count
            double atmTiming1 = getWalkingTimeByGoogleDirections(atmEntry1.getValue()) + atmEntry1.getValue().getLineCount()[0] * 90;
            double atmTiming2 = getWalkingTimeByGoogleDirections(atmEntry2.getValue()) + atmEntry2.getValue().getLineCount()[0] * 90;

            return Double.compare(atmTiming1, atmTiming2);
        };

        prioritizedMap = atmDB.getAllAtms().entrySet().stream()
                .sorted(valueComparator)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return prioritizedMap;
    }

    public Map<String, Atm> getLastFilteredAtms() {
        return lastFilteredAtms;
    }

    public void setLastFilteredAtms(Map<String, Atm> lastFilteredAtms) {
        this.lastFilteredAtms = lastFilteredAtms;
    }


    public interface AzureWorkflowService {
        @GET("/maps/api/directions/json")
        Call<String> getDirections(
                @Header("accept") String type,
                @Query("origin") String origin,
                @Query("destination") String destination,
                @Query("mode") String mode,
                @Query("key") String key
        );
    }

    private double getWalkingTimeByGoogleDirections(Atm atm) {
        String baseUrl = "https://maps.googleapis.com";

//        String url = baseUrl + "json?" +
//                "&origin=" + atmDB.location.getLatitude() + "," + atmDB.location.getLongitude() +
//                "&destination=" + atm.getLatitude() + "," + atm.getLongitude() +
//                "&mode=walking" +
//                "&key=AIzaSyA04qYRWy_mvo7Qbbjt0Y_7wz4xyzlz7SQ/";

        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .get()
                        .url(baseUrl)
                        .build();

                return chain.proceed(request);
            }
        };

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(200, TimeUnit.SECONDS)
                .writeTimeout(200, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
//                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(baseUrl)
                .build();

        AzureWorkflowService service = retrofit.create(AzureWorkflowService.class);

        final Call<String> authCall = service.getDirections(
                "application/json",
                atmDB.location.getLatitude() + "," + atmDB.location.getLongitude(),
                atm.getLatitude() + "," + atm.getLongitude(),
                "walking",
                "AIzaSyA04qYRWy_mvo7Qbbjt0Y_7wz4xyzlz7SQ"
        );

        Response<String> response = null;
        try {
            response = authCall.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert response != null;
        if (response.isSuccessful()) {
            Gson gson = new Gson();
            assert response.body() != null;
            Map generatedToken = gson.fromJson(response.body(), Map.class);


        } else {
            System.out.println("Failure");
        }
//        todo: завтра спросить апи ключ чтобы была возможность получать json
        return 0;
    }

    public Map<String, Atm> filterAtms(boolean doesWantToPut, boolean doesWantToTake, double amount) {
        if (doesWantToTake) {
            return atmDB.getAllAtms().entrySet().stream()
                    .filter(atmEntry -> atmEntry.getValue().getMoneyPresence() > amount)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            return atmDB.getAllAtms();
        }
    }


    public Map<String, Atm> getAtmsWithLocationPriority() {
        Map<String, Atm> prioritizedMap;

        Comparator<Map.Entry<String, Atm>> valueComparator = (atmEntry1, atmEntry2) -> {
            Atm atm1 = atmEntry1.getValue();
            Atm atm2 = atmEntry2.getValue();

            Location atmLocation1 = new Location(atm1.getId());
            atmLocation1.setLongitude(atm1.getLongitude());
            atmLocation1.setLatitude(atm1.getLatitude());

            Location atmLocation2 = new Location(atm2.getId());
            atmLocation2.setLongitude(atm2.getLongitude());
            atmLocation2.setLatitude(atm2.getLatitude());

            double atmDistance1 = location.distanceTo(atmLocation1);
            double atmDistance2 = location.distanceTo(atmLocation2);

            return Double.compare(atmDistance1, atmDistance2);
        };


        //todo: сейчас только по локации. надо добавить очереди и скорость хода
        prioritizedMap = atmDB.getAllAtms().entrySet().stream()
                .sorted(valueComparator)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return prioritizedMap;
    }
}
