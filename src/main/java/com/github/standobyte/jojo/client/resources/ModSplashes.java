package com.github.standobyte.jojo.client.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;

public class ModSplashes extends ReloadListener<List<String>> {
	private static final Random RANDOM = new Random();
	private final List<String> splashes = Lists.newArrayList();
	private final Session user;
	private final ResourceLocation location;

	public ModSplashes(Session session, ResourceLocation location) {
		this.user = session;
		this.location = location;
	}

	@Override
	protected List<String> prepare(IResourceManager resourceManager, IProfiler profiler) {
		try (
				IResource resource = Minecraft.getInstance().getResourceManager().getResource(location);
				BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
				) {
			return reader.lines().map(String::trim).filter((line) -> {
				return line.hashCode() != 125780783;
			}).collect(Collectors.toList());
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
	public String getSplash() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if (month == Calendar.DECEMBER && day == 24) {
			return "Merry X-mas!";
		} else if (month == Calendar.JANUARY && day == 1) {
			return "Happy new year!";
		} else if (month == Calendar.OCTOBER && day == 31) {
			return "ゴ ゴ ゴ ゴ ゴ ゴ ゴ ゴ ゴ ゴ";
		} else if (this.splashes.isEmpty()) {
			return null;
		} else {
			return this.splashes.get(RANDOM.nextInt(this.splashes.size()));
		}
	}
	
	public int getSplashesCount() {
		return splashes.size();
	}
}
