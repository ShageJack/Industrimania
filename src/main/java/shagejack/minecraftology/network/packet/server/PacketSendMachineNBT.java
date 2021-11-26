package shagejack.minecraftology.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import shagejack.minecraftology.machines.MachineNBTCategory;
import shagejack.minecraftology.network.packet.AbstractBiPacketHandler;
import shagejack.minecraftology.network.packet.TileEntityUpdatePacket;
import shagejack.minecraftology.tile.MCLTileEntity;

import java.util.EnumSet;

public class PacketSendMachineNBT extends TileEntityUpdatePacket {
    NBTTagCompound data;
    int cattegories;
    boolean forceUpdate;

    public PacketSendMachineNBT() {
    }

    public PacketSendMachineNBT(EnumSet<MachineNBTCategory> categories, MCLTileEntity tileEntity, boolean forceUpdate, boolean toDisk) {
        super(tileEntity);
        data = new NBTTagCompound();
        this.forceUpdate = forceUpdate;
        tileEntity.writeCustomNBT(data, categories, toDisk);
        this.cattegories = MachineNBTCategory.encode(categories);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        data = ByteBufUtils.readTag(buf);
        cattegories = buf.readInt();
        forceUpdate = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        ByteBufUtils.writeTag(buf, data);
        buf.writeInt(cattegories);
        buf.writeBoolean(forceUpdate);
    }

    public static class BiHandler extends AbstractBiPacketHandler<PacketSendMachineNBT> {
        @SideOnly(Side.CLIENT)
        @Override
        public void handleClientMessage(EntityPlayerSP player, PacketSendMachineNBT message, MessageContext ctx) {
            TileEntity tileEntity = message.getTileEntity(player.world);
            if (tileEntity instanceof MCLTileEntity) {
                ((MCLTileEntity) tileEntity).readCustomNBT(message.data, MachineNBTCategory.decode(message.cattegories));
            }
        }

        @Override
        public void handleServerMessage(EntityPlayerMP player, PacketSendMachineNBT message, MessageContext ctx) {
            TileEntity tileEntity = message.getTileEntity(player.world);
            if (tileEntity instanceof MCLTileEntity) {
                ((MCLTileEntity) tileEntity).readCustomNBT(message.data, MachineNBTCategory.decode(message.cattegories));
                if (message.forceUpdate) {
                   // if (tileEntity instanceof MCLTileEntityMachine) {
                   //     ((MCLTileEntityMachine) tileEntity).forceSync();
                    //}
                }
            }
        }
    }
}
