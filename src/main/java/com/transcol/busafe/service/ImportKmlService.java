package com.transcol.busafe.service;

import com.transcol.busafe.model.Rota;
import com.transcol.busafe.model.PontoRota;
import com.transcol.busafe.repository.PontoRotaRepository;
import com.transcol.busafe.repository.RotaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ImportKmlService {

  private final RotaRepository rotaRepo;
  private final PontoRotaRepository pontoRepo;

  public ImportKmlService(RotaRepository rotaRepo, PontoRotaRepository pontoRepo) {
    this.rotaRepo = rotaRepo;
    this.pontoRepo = pontoRepo;
  }

  @Transactional
  public int importar(InputStream kmlStream) throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(false);
    dbf.setExpandEntityReferences(false);
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse(kmlStream);

    NodeList placemarks = doc.getElementsByTagName("Placemark");
    int count = 0;

    Pattern cod = Pattern.compile("(\\d{3,4}[A-Za-z]?)");

    for (int i = 0; i < placemarks.getLength(); i++) {
      Element pm = (Element) placemarks.item(i);

      String name = getText(pm, "name");
      String desc = getText(pm, "description");
      String texto = (name + " " + desc).toUpperCase();

      String linhaTranscol = null, linhaMunicipal = null;
      Matcher m = cod.matcher(texto);
      if (m.find()) {
        String codigo = m.group(1);
        // heurística simples: se tem letra (ex: 500I) assumimos Transcol
        if (codigo.matches(".*[A-Za-z].*")) linhaTranscol = codigo;
        else linhaMunicipal = codigo;
      }

      String sentido = null;
      if (texto.contains("IDA")) sentido = "Ida";
      else if (texto.contains("VOLTA")) sentido = "Volta";

      Rota rota = new Rota();
      rota.setLinhaTranscol(linhaTranscol);
      rota.setLinhaMunicipal(linhaMunicipal);
      rota.setSentido(sentido);
      rota.setPlacemarkName(name);
      rotaRepo.save(rota);

      // coordinates pode aparecer em LineString ou MultiGeometry
      NodeList coords = pm.getElementsByTagName("coordinates");
      int ordem = 1;
      List<PontoRota> buffer = new ArrayList<>();

      for (int j = 0; j < coords.getLength(); j++) {
        String txt = coords.item(j).getTextContent();
        if (txt == null || txt.isBlank()) continue;
        String[] pares = txt.trim().replace("\n", " ").replace("\r", " ").split("\\s+");
        for (String par : pares) {
          String[] xy = par.split(",");
          if (xy.length >= 2) {
            try {
              double lon = Double.parseDouble(xy[0].trim());
              double lat = Double.parseDouble(xy[1].trim());
              PontoRota p = new PontoRota();
              p.setRota(rota);
              p.setOrdem(ordem++);
              p.setLat(lat);
              p.setLon(lon);
              buffer.add(p);
              if (buffer.size() >= 1000) { pontoRepo.saveAll(buffer); buffer.clear(); }
            } catch (NumberFormatException ignore) {}
          }
        }
      }
      if (!buffer.isEmpty()) pontoRepo.saveAll(buffer);
      count++;
    }
    return count;
  }

  private static String getText(Element el, String tag) {
    NodeList nl = el.getElementsByTagName(tag);
    return nl.getLength() > 0 ? nl.item(0).getTextContent() : "";
  }
}
