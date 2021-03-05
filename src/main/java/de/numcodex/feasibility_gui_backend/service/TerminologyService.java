package de.numcodex.feasibility_gui_backend.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.feasibility_gui_backend.model.ui.CategoryEntry;
import de.numcodex.feasibility_gui_backend.model.ui.TerminologyEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class TerminologyService {

  private final String uiProfilePath;
  private static final List<String> SORTED_CATEGORIES =
      List.of("Anamnese / Risikofaktoren", "Demographie", "Laborwerte", "Therapie", "Andere");
  private final Map<UUID, TerminologyEntry> terminologyEntries = new HashMap<>();
  private final List<CategoryEntry> categoryEntries = new ArrayList<>();
  private final Map<UUID, TerminologyEntry> terminologyEntriesWithOnlyDirectChildren =
      new HashMap<>();
  private final Set<TerminologyEntryWithCategory> selectableEntriesWithCategory = new HashSet<>();

  public TerminologyService(@Value("${backend.ontology-folder}") String uiProfilePath) {
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
        var terminologyEntry = (objectMapper.readValue(
                new URL("file:" + uiProfilePath + "/" + filename), TerminologyEntry.class));
        terminologyEntries.put(terminologyEntry.getId(), terminologyEntry);
        categoryEntries.add(new CategoryEntry(terminologyEntry));
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

  private Set<TerminologyEntry> extractSelectableEntries(TerminologyEntry terminologyEntry) {
    Set<TerminologyEntry> selectableEntries = new HashSet<>();
    if (terminologyEntry.isSelectable()) {
      selectableEntries.add(TerminologyEntry.copyWithoutChildren(terminologyEntry));
    }
    for (var child : terminologyEntry.getChildren()) {
      selectableEntries.addAll(extractSelectableEntries(child));
    }
    return selectableEntries;
  }

  private void generateSelectableEntriesByCategory() {
    for (var categoryTerminologyEntry : terminologyEntries.values()) {
      CategoryEntry categoryEntry = new CategoryEntry(categoryTerminologyEntry);

      extractSelectableEntries(categoryTerminologyEntry).forEach(entry ->
              selectableEntriesWithCategory.add(new TerminologyEntryWithCategory(entry, categoryEntry)));
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
    return selectableEntriesWithCategory.stream()
          .filter(terminologyEntryWithCategory ->
                  matchesCategoryId(categoryId, terminologyEntryWithCategory.getCategoryEntry()))
          .map(entry -> TerminologyEntry.copyWithoutChildren(entry.getTerminologyEntry()))
          .filter((terminologyEntry -> matchesQuery(query, terminologyEntry)))
          .collect(Collectors.toList());
  }

  private boolean matchesCategoryId(UUID categoryId, CategoryEntry categoryEntry) {
    return categoryEntry.getCatId().equals(categoryId) || categoryId == null;
  }

  private boolean matchesQuery(String query, TerminologyEntry terminologyEntry) {
    return terminologyEntry.getDisplay().toLowerCase().contains(query.toLowerCase()) ||
        (terminologyEntry.getTermCode() != null && terminologyEntry.getTermCode().getCode()
            .contains(query));
  }

  @AllArgsConstructor
  @Getter
  private static class TerminologyEntryWithCategory {
    private final TerminologyEntry terminologyEntry;
    private final CategoryEntry categoryEntry;
  }
}
