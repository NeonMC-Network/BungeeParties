package net.simplyrin.bungeeparties.utils;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.simplyrin.bungeeparties.Main;
import net.simplyrin.bungeeparties.exceptions.*;
import net.simplyrin.bungeeparties.utils.LanguageManager.LanguageUtils;

import java.util.ArrayList;
import java.util.List;
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
public class PartyManager {

	private Main plugin;

	public PartyManager(Main plugin) {
		this.plugin = plugin;
	}

	public PartyUtils getPlayer(ProxiedPlayer player) {
		return new PartyUtils(player.getUniqueId());
	}

	public PartyUtils getPlayer(String uuid) {
		return new PartyUtils(UUID.fromString(uuid));
	}

	public PartyUtils getPlayer(UUID uniqueId) {
		return new PartyUtils(uniqueId);
	}

	public class PartyUtils {

		private UUID uuid;

		public PartyUtils(UUID uuid) {
			this.uuid = uuid;

			if(PartyManager.this.plugin.getConfigManager().getConfig().get("Player." + this.uuid.toString()) == null) {
				ProxiedPlayer player = PartyManager.this.plugin.getProxy().getPlayer(this.uuid);

				PartyManager.this.plugin.info("Creating data for player " + player.getName() + "...");

				PartyManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Name", player.getName());
				PartyManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Language", "english");
				PartyManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Currently-Joined-Party", "NONE");
				PartyManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Party-List", new ArrayList<>());
				PartyManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Requests", new ArrayList<>());
				PartyManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Toggle", true);
			}
		}

		public ProxiedPlayer getPlayer() {
			try {
				return PartyManager.this.plugin.getProxy().getPlayer(this.uuid);
			} catch (Exception e) {
			}
			return null;
		}

		/**
		 * @return Party leader
		 */
		public PartyUtils leaveCurrentParty(LanguageUtils langUtils) {
			PartyUtils leader = PartyManager.this.plugin.getPartyManager().getPlayer(PartyManager.this.plugin.getConfigManager().getConfig().getString("Player." + this.uuid.toString() + ".Currently-Joined-Party"));
			try {
				leader.remove(this.uuid, langUtils);
			} catch (NotJoinedException e) {
			}
			return leader;
		}

		public boolean isJoinedParty() {
			String currentlyJoinedParty = PartyManager.this.plugin.getConfigManager().getConfig().getString("Player." + this.uuid.toString() + ".Currently-Joined-Party");
			return !currentlyJoinedParty.equals("NONE");
		}

		public PartyUtils getPartyLeader(LanguageUtils langUtils) throws NotJoinedException {
			String currentlyJoinedParty = PartyManager.this.plugin.getConfigManager().getConfig().getString("Player." + this.uuid.toString() + ".Currently-Joined-Party");
			if(currentlyJoinedParty.equals("NONE")) {
				throw new NotJoinedException(langUtils.getString("Exceptions.Not-Joined"));
			}
			return PartyManager.this.plugin.getPartyManager().getPlayer(currentlyJoinedParty);
		}

		public boolean isPartyOwner(LanguageUtils langUtils) throws NotJoinedException {
			return this.uuid == this.getPartyLeader(langUtils).getPlayer().getUniqueId();
		}

		public PartyUtils setEnabledReceiveRequest(boolean bool) {
			PartyManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Toggle", bool);
			return this;
		}

		public boolean isEnabledReceiveRequest() {
			return PartyManager.this.plugin.getConfigManager().getConfig().getBoolean("Player." + this.uuid.toString() + ".Toggle");
		}

		public UUID getUniqueId() {
			return this.uuid;
		}

		public String getDisplayName() {
			return PartyManager.this.plugin.getNameManager().getPlayer(this.uuid).getDisplayName();
		}

		public PartyUtils addRequest(ProxiedPlayer player, LanguageUtils langUtils) throws AlreadyJoinedException, FailedInvitingException {
			return this.addRequest(player.getUniqueId(), langUtils);
		}

		public PartyUtils addRequest(UUID uuid, LanguageUtils langUtils) throws AlreadyJoinedException, FailedInvitingException {
			if(this.uuid.toString().equals(uuid.toString())) {
				throw new FailedInvitingException(langUtils.getString("Exceptions.Cant-Invite-Self"));
			}

			if(!PartyManager.this.plugin.getConfigManager().getConfig().getBoolean("Player." + uuid.toString() + ".Toggle")) {
				throw new FailedInvitingException(langUtils.getString("Exceptions.Disable-Invite").replace("%targetDisplayName", PartyManager.this.plugin.getNameManager().getPlayer(uuid).getDisplayName()));
			}

			List<String> list = this.getParties();
			if(list.contains(uuid.toString())) {
				throw new AlreadyJoinedException(langUtils.getString("Exceptions.Already.In").replace("%targetDisplayName", PartyManager.this.plugin.getNameManager().getPlayer(uuid).getDisplayName()));
			}

			if(!PartyManager.this.plugin.getConfigManager().getConfig().getString("Player." + uuid.toString() + ".Currently-Joined-Party").equals("NONE")) {
				throw new AlreadyJoinedException(langUtils.getString("Exceptions.Already.Joined"));
			}

			List<String> requests = PartyManager.this.plugin.getConfigManager().getConfig().getStringList("Player." + this.uuid.toString() + ".Requests");
			if(requests.contains(uuid.toString())) {
				throw new FailedInvitingException(langUtils.getString("Exceptions.Already.Invited").replace("%targetDisplayName", PartyManager.this.plugin.getNameManager().getPlayer(uuid).getDisplayName()));
			}
			requests.add(uuid.toString());
			PartyManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Requests", requests);
			return this;
		}

		public PartyUtils removeRequest(ProxiedPlayer player, LanguageUtils langUtils) throws NotInvitedException {
			return this.removeRequest(player.getUniqueId(), langUtils);
		}

		public PartyUtils removeRequest(UUID uuid, LanguageUtils langUtils) throws NotInvitedException {
			List<String> requests = PartyManager.this.plugin.getConfigManager().getConfig().getStringList("Player." + this.uuid.toString() + ".Requests");
			if(!requests.contains(uuid.toString())) {
				throw new NotInvitedException(langUtils.getString("Exceptions.Havent-Received-Invite"));
			}
			requests.remove(uuid.toString());
			PartyManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Requests", requests);
			return this;
		}

		public List<String> getParties() {
			return PartyManager.this.plugin.getConfigManager().getConfig().getStringList("Player." + this.uuid.toString() + ".Party-List");
		}

		public PartyUtils add(ProxiedPlayer player, LanguageUtils langUtils) throws AlreadyJoinedException, FailedAddingException {
			return this.add(player.getUniqueId(), langUtils);
		}

		public PartyUtils add(UUID uuid, LanguageUtils langUtils) throws AlreadyJoinedException, FailedAddingException {
			if(this.uuid.toString().equals(uuid.toString())) {
				throw new FailedAddingException(langUtils.getString("Exceptions.Your-Self-Invite"));
			}

			List<String> list = this.getParties();
			if(list.contains(uuid.toString())) {
				throw new AlreadyJoinedException(langUtils.getString("Exceptions.Already.In").replace("%targetDisplayName", PartyManager.this.plugin.getNameManager().getPlayer(uuid).getDisplayName()));
			}
			list.add(uuid.toString());
			PartyManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Party-List", list);
			PartyManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Currently-Joined-Party", this.uuid.toString());

			PartyManager.this.plugin.getConfigManager().getConfig().set("Player." + uuid.toString() + ".Currently-Joined-Party", this.uuid.toString());
			return this;
		}

		public PartyUtils remove(ProxiedPlayer player, LanguageUtils langUtils) throws NotJoinedException {
			return this.remove(player.getUniqueId(), langUtils);
		}

		public PartyUtils remove(UUID uuid, LanguageUtils langUtils) throws NotJoinedException {
			if(this.uuid.toString().equals(uuid.toString())) {
				throw new NotJoinedException("&cYou can't remove yourself! Use /party disaband instead!");
			}

			List<String> list = this.getParties();
			if(!list.contains(uuid.toString())) {
				throw new NotJoinedException(langUtils.getString("Exceptions.Isnt-In-Your-Party").replace("%targetDisplayName", PartyManager.this.plugin.getNameManager().getPlayer(uuid).getDisplayName()));
			}
			list.remove(uuid.toString());
			PartyManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Party-List", list);
			PartyManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Currently-Joined-Party", "NONE");

			PartyManager.this.plugin.getConfigManager().getConfig().set("Player." + uuid.toString() + ".Currently-Joined-Party", "NONE");
			return this;
		}

	}

}
