package swim.covid;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import swim.api.ref.SwimRef;
import swim.structure.Record;

import java.io.*;
import java.util.*;

class DataSource  {

    private final SwimRef ref;
    private final String hostUri;

    DataSource(SwimRef ref, String hostUri) {
        this.ref = ref;
        this.hostUri = hostUri;
    }

    void sendCommands() throws InterruptedException, FileNotFoundException {
        System.out.println("parsing start");
        int buffer = 10;
        while(true) {
            CsvParserSettings settings = new CsvParserSettings();
            settings.detectFormatAutomatically();
            CsvParser parser = new CsvParser(settings);
            List<String[]> allRow = parser.parseAll(getReader("covidcase.csv"));
            HashMap<String, Integer> overallMap = new HashMap<String, Integer>();
            HashMap<String, Integer> confirmedMap = new HashMap<String, Integer>();
            HashMap<String, Integer> deathsMap = new HashMap<String, Integer>();
            Integer tempVal = 0;
            String type = "";
            for (String[] record : allRow) {
                type = record[0];
                if (!type.toLowerCase().equals("case_type")) {
                    if (overallMap.get(type) != null) {
                        tempVal = 0;
                        tempVal = overallMap.get(type);
                        int value = tempVal + Integer.parseInt(record[2]) + buffer;
                        overallMap.put(type, value);
                    } else {
                        overallMap.put(type, Integer.parseInt(record[2]));
                    }
                    if (type.toLowerCase().equals("confirmed")) {
                        if (confirmedMap.get(record[6]) != null) {
                            tempVal = 0;
                            tempVal = confirmedMap.get(record[6]);
                            int value = tempVal + Integer.parseInt(record[2]) + buffer;
                            confirmedMap.put(record[6], value);
                        } else {
                            confirmedMap.put(record[6], Integer.parseInt(record[2]));
                        }
                    } else {
                        if (deathsMap.get(record[6]) != null) {
                            tempVal = 0;
                            tempVal = deathsMap.get(record[6]);
                            int value = tempVal + Integer.parseInt(record[2]) + buffer;
                            deathsMap.put(record[6], value);
                        } else {
                            deathsMap.put(record[6], Integer.parseInt(record[2]));
                        }
                    }
                }
            }
            overallMap = sortByValues(overallMap);
            confirmedMap = sortByValues(confirmedMap);
            deathsMap = sortByValues(deathsMap);

            Record oveeall = Record.create(overallMap.size());
            for (Map.Entry overAllElement : overallMap.entrySet()) {
                String key = (String) overAllElement.getKey();
                Integer val = (Integer) overAllElement.getValue();
                oveeall.slot(key, val);
            }
            Integer count = 1;
            Record confirmed = Record.create(confirmedMap.size());
            for (Map.Entry confirmedElement : confirmedMap.entrySet()) {
                String key = (String) confirmedElement.getKey();
                Integer val = (Integer) confirmedElement.getValue();
                confirmed.slot(key, val);
                if (count == 6) {
                    break;
                }
                count = count + 1;
            }
            count = 1;
            Record deaths = Record.create(deathsMap.size());
            for (Map.Entry deathsElement : deathsMap.entrySet()) {
                String key = (String) deathsElement.getKey();
                Integer val = (Integer) deathsElement.getValue();
                deaths.slot(key, val);
                if (count == 6) {
                    break;
                }
                count = count + 1;
            }
            System.out.println(oveeall);
            System.out.println(confirmed);
            System.out.println(deaths);

            this.ref.command(this.hostUri, "/covid/overall", "publish", oveeall);
            this.ref.command(this.hostUri, "/covid/confirmed", "publish", confirmed);
            this.ref.command(this.hostUri, "/covid/deaths", "publish", deaths);
            buffer += 5;
            Thread.sleep(3000);

        }
    }
    public static Reader getReader(String relativePath) throws FileNotFoundException {
        try {
            InputStream is = new FileInputStream(relativePath);
            return new InputStreamReader(is);
        } catch (Exception e) {

            throw e;
        }
    }
    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }
}
