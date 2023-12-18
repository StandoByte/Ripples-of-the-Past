package com.github.standobyte.jojo.client.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;

public class ModSplashes extends ReloadListener<List<String>> {
    private static final Random RANDOM = new Random();
    private final List<String> splashes = new ArrayList<>();
    private final Session user;
    private final ResourceLocation location;

    public ModSplashes(Session session, ResourceLocation location) {
        this.user = session;
        this.location = location;
    }

    @Override
    protected List<String> prepare(IResourceManager resourceManager, IProfiler profiler) {
        try (
                IResource resource = resourceManager.getResource(location);
                BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
                ) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> line.length() > 0 && !line.startsWith("//"))
                    .collect(Collectors.toList());
        } catch (IOException ioexception) {
            return Collections.emptyList();
        }
    }

    @Override
    protected void apply(List<String> splashes, IResourceManager resourceManager, IProfiler profiler) {
        this.splashes.clear();
        this.splashes.addAll(splashes);
    }
    
    @Nullable
    public String overrideSplash() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (month == Calendar.DECEMBER && day == 24 || month == Calendar.JANUARY && day == 1) {
            return null;
        }
        if (month == Calendar.OCTOBER && day == 31 && RANDOM.nextInt(50) == 0) {
            return "ゴ ゴ ゴ ゴ ゴ ゴ ゴ ゴ ゴ ゴ";
        }
        if (!splashes.isEmpty() && RANDOM.nextInt(420 + splashes.size()) < splashes.size()) {
            String splash = splashes.get(RANDOM.nextInt(splashes.size()));
            splash = splash.replace("@p", user.getName());
            return splash;
        }
        return null;
    }
}
