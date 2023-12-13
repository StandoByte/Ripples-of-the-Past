package com.github.standobyte.jojo.client.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.general.MathUtil;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;

public class ModSplashes extends ReloadListener<List<String>> {
    private static final Random RANDOM = new Random();
    private final Object2IntMap<String> splashes = new Object2IntArrayMap<>();
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

    private static final Pattern WEIGHT_PATTERN = Pattern.compile("^\\(\\d+\\) ");
    @Override
    protected void apply(List<String> splashes, IResourceManager resourceManager, IProfiler profiler) {
        this.splashes.clear();
        for (String line : splashes) {
            int weight = 1;
            Matcher matcher = WEIGHT_PATTERN.matcher(line);
            if (matcher.find()) {
                String weightStr = matcher.group();
                try {
                    weight = Integer.parseInt(weightStr.replaceAll("[\\D]", ""));
                    line = line.replace(weightStr, "");
                }
                catch (NumberFormatException e) {}
            }
            this.splashes.put(line, weight);
        }
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
            Optional<String> splashOpt = MathUtil.getRandomWeightedInt(splashes.keySet(), splashes::applyAsInt, RANDOM);
            return splashOpt.map(splash -> {
                return user != null ? splash.replace("@p", user.getName()) : splash;
            }).orElse(null);
        }
        return null;
    }
}
