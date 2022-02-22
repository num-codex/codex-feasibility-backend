package de.numcodex.feasibility_gui_backend.terminology;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.feasibility_gui_backend.terminology.api.CategoryEntry;
import de.numcodex.feasibility_gui_backend.terminology.api.TerminologyEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TerminologyService {

  private  String uiProfilePath;
  private static final List<String> SORTED_CATEGORIES = List.of("Einwilligung", "Biobank", "Diagnose", "Fall", "Laborbefund", "Medikation", "Person", "Prozedur", "GECCO");
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
      var terminologyEntryWithoutChildren = TerminologyEntry.copyWithoutChildren(terminologyEntry);
      selectableEntries.add(terminologyEntryWithoutChildren);
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
          .filter((terminologyEntry -> matchesQuery(query, terminologyEntry))).sorted((Comparator
              .comparingInt(val -> val.getDisplay().length()))).limit(20)
          .collect(Collectors.toList());
    } else {
      Set<TerminologyEntry> allSelectableEntries = new HashSet<>();
      for (var selectableEntries : selectableEntriesByCategory.values()) {
        allSelectableEntries.addAll(selectableEntries);
      }
      System.out.println(allSelectableEntries.size());
      return allSelectableEntries.stream()
          .filter((terminologyEntry -> matchesQuery(query, terminologyEntry))).sorted((Comparator
              .comparingInt(val -> val.getDisplay().length()))).limit(20)
          .collect(Collectors.toList());
    }
  }

  public static int min(int... numbers) {
    return Arrays.stream(numbers)
        .min().orElse(Integer.MAX_VALUE);
  }


  private boolean matchesQuery(String query, TerminologyEntry terminologyEntry) {
    return terminologyEntry.getDisplay().toLowerCase().startsWith(query.toLowerCase()) ||
        Arrays.stream(terminologyEntry.getDisplay().toLowerCase().split(" "))
        .anyMatch(var-> var.startsWith(query.toLowerCase())) ||
        (terminologyEntry.getTermCode() != null && terminologyEntry.getTermCode().getCode()
            .startsWith(query));
  }
}
