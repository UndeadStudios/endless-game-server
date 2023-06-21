package com.zenyte.game.content.boss.grotesqueguardians.plugins;

import com.zenyte.game.tasks.WorldTasksManager;
import com.zenyte.game.world.Projectile;
import com.zenyte.game.world.World;
import com.zenyte.game.world.entity.player.Player;
import com.zenyte.game.world.object.ObjectAction;
import com.zenyte.game.world.object.WorldObject;

/**
 * @author Tommeh | 01/08/2019 | 21:38
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>
 */
public class EnergySphereObject implements ObjectAction {
    @Override
    public void handleObjectAction(Player player, WorldObject object, String name, int optionId, String option) {
        final java.util.Optional<com.zenyte.game.content.boss.grotesqueguardians.instance.GrotesqueGuardiansInstance> optionalInstance = player.getGrotesqueGuardiansInstance();
        if (!optionalInstance.isPresent()) {
            return;
        }
        final com.zenyte.game.content.boss.grotesqueguardians.instance.GrotesqueGuardiansInstance instance = optionalInstance.get();
        final com.zenyte.game.content.boss.grotesqueguardians.EnergySphere sphere = instance.getEnergySphere(object.getId());
        if (sphere == null) {
            return;
        }
        final com.zenyte.game.world.Projectile projectile = new Projectile(1437 + sphere.getState().ordinal(), 0, 50, 20, 0);
        World.sendProjectile(object, player, projectile);
        World.removeObject(sphere);
        WorldTasksManager.schedule(sphere::absorb, projectile.getTime(object, player));
    }

    @Override
    public Object[] getObjects() {
        return new Object[] {31686, 31687, 31688};
    }
}
