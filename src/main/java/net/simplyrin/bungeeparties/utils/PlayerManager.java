package net.simplyrin.bungeeparties.utils;

import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeeparties.Main;
import net.simplyrin.bungeefriends.tools.Config;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by SimplyRin on 2018/07/31.
 *
 * Copyright (c) 2018 SimplyRin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class PlayerManager {

	private Main plugin;
	private Runnable runnable;
	private Configuration config;

	public PlayerManager(Main plugin) {
		this.plugin = plugin;

		this.createConfig();
		this.saveAndReload();
	}

	public UUID getPlayerUniqueId(String name) {
		String string = this.config.getString("Name." + name.toLowerCase());
		if(string.equals("NONE")) {
			return null;
		}
		if(string.length() != 36) {
			return null;
		}
		try {
			return UUID.fromString(string);
		} catch (Exception ignored) {
		}
		return null;
	}

	String getPlayerName(UUID uuid) {
		return this.config.getString("UUID." + uuid.toString());
	}

	public void saveAndReload() {
		File config = new File(this.plugin.getDataFolder(), "player.yml");

		Config.saveConfig(this.config, config);
		this.config = Config.getConfig(config);
	}

	private void createConfig() {
		File folder = this.plugin.getDataFolder();
		if(!folder.exists()) {
			folder.mkdir();
		}

		File config = new File(folder, "player.yml");
		if(!config.exists()) {
			try {
				config.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.config = Config.getConfig(config);

			this.config.set("UUID.b0bb65a2-832f-4a5d-854e-873b7c4522ed", "SimplyRin");
			this.config.set("Name.simplyrin", "b0bb65a2-832f-4a5d-854e-873b7c4522ed");

			this.config.set("UUID.64636120-8633-4541-aa5f-412b42ddb04d", "SimplyFoxy");
			this.config.set("Name.simplyfoxy", "64636120-8633-4541-aa5f-412b42ddb04d");

			this.config.set("UUID.cbf39561-d020-4d22-bc71-f1f0ce784314", "Naptie");
			this.config.set("Name.siderrmc", "cbf39561-d020-4d22-bc71-f1f0ce784314");

			Config.saveConfig(this.config, config);
		}

		this.config = Config.getConfig(config);
		this.saveAndReload();
	}

	public Configuration getConfig() {
		return config;
	}
}
