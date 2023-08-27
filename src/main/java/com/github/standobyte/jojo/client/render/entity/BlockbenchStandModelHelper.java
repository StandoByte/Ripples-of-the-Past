package com.github.standobyte.jojo.client.render.entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;
import com.github.standobyte.jojo.entity.stand.StandEntity;

public class BlockbenchStandModelHelper {

    public static <T extends StandEntity, M extends HumanoidStandModel<T>> M organizeHumanoidModelParts(M exportedModel) {
        Field[] humanoidModelParts = HumanoidStandModel.class.getDeclaredFields();
        List<Field> exportedModelParts = new ArrayList<>();
        Collections.addAll(exportedModelParts, exportedModel.getClass().getDeclaredFields());
        for (Field humanoidPartField : humanoidModelParts) {
            String name = humanoidPartField.getName();
            Optional<Field> exportedFieldSameName = exportedModelParts.stream().filter(field -> field.getName().equals(name)).findAny();
            if (exportedFieldSameName.isPresent()) {
                Field exportedPartField = exportedFieldSameName.get();
                humanoidPartField.setAccessible(true);
                exportedPartField.setAccessible(true);
                try {
                    humanoidPartField.set(exportedModel, exportedPartField.get(exportedModel));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    JojoMod.getLogger().error("Failed to organize humanoid Stand model parts for model {}", exportedModel.getClass().getName());
                    e.printStackTrace();
                    return exportedModel;
                }
            }
        }
        return exportedModel;
    }
}
