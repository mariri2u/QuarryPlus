package org.yogpstop.qp;

import net.minecraft.src.TileEntity;
import net.minecraft.src.StatCollector;
import buildcraft.api.gates.Trigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.DefaultProps;

public class TriggerPlusMachine extends Trigger {

	boolean active;

	public TriggerPlusMachine(int pid, boolean active) {
		super(pid);
		this.active = active;
	}

	@Override
	public String getDescription() {
		if (this.active) return StatCollector.translateToLocal("trigger.plus_working");
		return StatCollector.translateToLocal("trigger.plus_done");
	}

	@Override
	public boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter) {
		if (tile instanceof TileQuarry) {
			if (this.active) return ((TileQuarry) tile).G_getNow() != TileQuarry.NONE;
			return ((TileQuarry) tile).G_getNow() == TileQuarry.NONE;
		} else if (tile instanceof TileMiningWell) {
			if (this.active) return ((TileMiningWell) tile).G_isWorking();
			return !((TileMiningWell) tile).G_isWorking();
		}
		return false;
	}

	@Override
	public int getIndexInTexture() {
		if (this.active) return 4 * 16 + 0;
		return 4 * 16 + 1;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_TRIGGERS;
	}

	@Override
	public boolean hasParameter() {
		return false;
	}
}
