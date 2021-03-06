package de.numcodex.feasibility_gui_backend.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.feasibility_gui_backend.model.ui.CategoryEntry;
import de.numcodex.feasibility_gui_backend.model.ui.TerminologyEntry;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TerminologyService {

  private  String uiProfilePath;
  private static final List<String> SORTED_CATEGORIES = List.of("Einwilligung", "Anamnese / Risikofaktoren", "Bioproben", "Demographie",
      "Laborwerte", "Therapie", "Andere");
  private Map<UUID, TerminologyEntry> terminologyEntries = new HashMap<>();
  private List<CategoryEntry> categoryEntries = new ArrayList<>();
  private Map<UUID, TerminologyEntry> terminologyEntriesWithOnlyDirectChildren = new HashMap<>();
  private Map<UUID, Set<TerminologyEntry>> selectableEntriesByCategory = new HashMap<>();

  public TerminologyService(@Value("${app.ontologyFolder}") String uiProfilePath) {
    this.uiProfilePath = uiProfilePath;
    readInTerminologyEntries();
    generateTerminologyEntriesWithoutDirectChildren();
    generateSelectableEntriesByCategory();
  }

  private void readInTerminologyEntries() {
    var files = getFilePathsUiProfiles();

    for (var filename : files) {
      var objectMapper = new ObjectMapper();
      objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
      try {
        var terminology_entry = (objectMapper.readValue(
            new URL("file:" + uiProfilePath + "/" + filename),
            TerminologyEntry.class));
        terminologyEntries.put(terminology_entry.getId(), terminology_entry);
        categoryEntries.add(new CategoryEntry(terminology_entry.getId(),
            terminology_entry.getDisplay()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void generateTerminologyEntriesWithoutDirectChildren() {
    for (var entry : terminologyEntries.values()) {
      generateTerminologyEntriesWithOnlyDirectChildren(entry);
    }
  }

  private Set<String> getFilePathsUiProfiles() {
    System.out.println("####################");
    System.out.println(uiProfilePath);

    return Stream.of(
        Objects.requireNonNull(new File(uiProfilePath).listFiles()))
        .filter(file -> !file.isDirectory())
        .map(File::getName)
        .collect(Collectors.toSet());
  }

  private void generateTerminologyEntriesWithOnlyDirectChildren(TerminologyEntry terminologyTree) {


    var entryWithOnlyDirectChildren = TerminologyEntry.copyWithDirectChildren(terminologyTree);
    terminologyEntriesWithOnlyDirectChildren
        .put(entryWithOnlyDirectChildren.getId(), entryWithOnlyDirectChildren);
    for (var child : terminologyTree.getChildren()) {
      generateTerminologyEntriesWithOnlyDirectChildren(child);
    }
  }

  private Set<TerminologyEntry> getSelectableEntries(TerminologyEntry terminologyEntry) {
    Set<TerminologyEntry> selectableEntries = new HashSet<>();
    if (terminologyEntry.isSelectable()) {
      selectableEntries.add(terminologyEntry);
    }
    for (var child : terminologyEntry.getChildren()) {
      selectableEntries.addAll(getSelectableEntries(child));
    }
    return selectableEntries;
  }

  private void generateSelectableEntriesByCategory() {
    for (var terminologyEntry : terminologyEntries.values()) {
      selectableEntriesByCategory
          .put(terminologyEntry.getId(), getSelectableEntries(terminologyEntry));
    }
  }

  //TODO: Unknown key!
  public TerminologyEntry getEntry(UUID nodeId) {
    return terminologyEntriesWithOnlyDirectChildren.get(nodeId);
  }

  public List<CategoryEntry> getCategories() {
    categoryEntries.sort(Comparator.comparing(value -> SORTED_CATEGORIES.indexOf(value.getDisplay())));
    return categoryEntries;
  }

  public List<TerminologyEntry> getSelectableEntries(String query, UUID categoryId) {
    if (categoryId != null) {
      return selectableEntriesByCategory.get(categoryId).stream()
          .filter((terminologyEntry -> matchesQuery(query, terminologyEntry)))
          .collect(Collectors.toList());
    } else {
      Set<TerminologyEntry> allSelectableEntries = new HashSet<>();
      for (var selectableEntries : selectableEntriesByCategory.values()) {
        allSelectableEntries.addAll(selectableEntries);
      }
      return allSelectableEntries.stream()
          .filter((terminologyEntry -> matchesQuery(query, terminologyEntry)))
          .collect(Collectors.toList());
    }
  }

  private boolean matchesQuery(String query, TerminologyEntry terminologyEntry) {
    return terminologyEntry.getDisplay().toLowerCase().contains(query.toLowerCase()) ||
        (terminologyEntry.getTermCode() != null && terminologyEntry.getTermCode().getCode()
            .contains(query));
  }
}
