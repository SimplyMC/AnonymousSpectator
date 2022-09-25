package net.simplymc.anonymousspectator;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.ListIterator;

public final class AnonymousSpectatorPlugin extends JavaPlugin {
    private ProtocolManager protoManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.protoManager = ProtocolLibrary.getProtocolManager();
        this.protoManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player receiver = event.getPlayer();
                EnumWrappers.PlayerInfoAction action = packet.getPlayerInfoAction().read(0);

                if (!action.equals(EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE) && !action.equals(EnumWrappers.PlayerInfoAction.ADD_PLAYER)) return;

                PacketContainer newPacket = packet.shallowClone();
                List<PlayerInfoData> dataList = newPacket.getPlayerInfoDataLists().read(0);
                ListIterator<PlayerInfoData> iterator = dataList.listIterator();
                while (iterator.hasNext()) {
                    PlayerInfoData data = iterator.next();

                    if (data.getGameMode() != EnumWrappers.NativeGameMode.SPECTATOR) continue;

                    Player playerToUpdate = getServer().getPlayer(data.getProfile().getUUID());

                    if (
                            playerToUpdate.hasPermission("anonspec.hide")
                            && !playerToUpdate.getUniqueId().equals(receiver.getUniqueId())
                            && !receiver.hasPermission("anonspec.bypass")
                    ) {
                        iterator.set(new PlayerInfoData(data.getProfile(), data.getLatency(), EnumWrappers.NativeGameMode.SURVIVAL, data.getDisplayName(), data.getProfileKeyData()));
                    }
                }

                newPacket.getPlayerInfoDataLists().write(0, dataList);
                event.setPacket(newPacket);
            }
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
