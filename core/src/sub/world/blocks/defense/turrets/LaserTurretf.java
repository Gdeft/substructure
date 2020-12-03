package sub.world.blocks.defense.turrets;

import arc.math.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import mindustry.world.meta.values.*;

import static mindustry.Vars.*;

public class LaserTurretf extends PowerTurret{
    public float firingMoveFract = 0.25f;
    public float shootDuration = 100f;

    public LaserTurretf(String name){
        super(name);
        canOverdrive = false;

        consumes.add(new ConsumeLiquidFilter(liquid -> liquid.temperature <= 0.5f && liquid.flammability < 0.1f, 0.01f)).update(false);
        coolantMultiplier = 1f;
    }

    @Override
    public void init(){
        consumes.powerCond(powerUse, entity -> ((LaserTurretfBuild)entity).bullet != null || ((LaserTurretfBuild)entity).target != null);
        super.init();
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.booster);
        stats.add(Stat.input, new BoosterListValue(reloadTime, consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount, coolantMultiplier, false, l -> consumes.liquidfilters.get(l.id)));
        stats.remove(Stat.damage);
        stats.add(Stat.damage, shootType.damage * 60f / 5f, StatUnit.perSecond);
    }

    public class LaserTurretfBuild extends PowerTurretBuild{
        Bullet bullet;
        float bulletLife;

        @Override
        protected void updateCooling(){
            //do nothing, cooling is irrelevant here
        }

        @Override
        public void updateTile(){
            super.updateTile();

            if(bulletLife > 0 && bullet != null){
                wasShooting = true;
                tr.trns(rotation, size * tilesize / 2f, 0f);
                bullet.rotation(rotation);
                bullet.set(x + tr.x, y + tr.y);
                bullet.time(0f);
                heat = 1f;
                recoil = recoilAmount;
                bulletLife -= Time.delta / Math.max(efficiency(), 0.00001f);
                if(bulletLife <= 0f){
                    bullet = null;
                }
            }else if(reload > 0){
                wasShooting = true;
                Liquid liquid = liquids.current();
                float maxUsed = consumes.<ConsumeLiquidBase>get(ConsumeType.liquid).amount;

                float used = (cheating() ? maxUsed * Time.delta : Math.min(liquids.get(liquid), maxUsed * Time.delta)) * liquid.heatCapacity * coolantMultiplier;
                reload -= used;
                liquids.remove(liquid, used);

                if(Mathf.chance(0.06 * used)){
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }
        }

        protected void shoot(BulletType type){
            useAmmo();

            tr.trns(rotation, size * tilesize / 2f);
            chargeBeginEffect.at(x, y, rotation);
            chargeSound.at(x, y, 1);

            for(int i = 0; i < chargeEffects; i++){
                Time.run(Mathf.random(chargeMaxDelay), () -> {
                    if(!isValid()) return;
                    tr.trns(rotation, size * tilesize / 2f);
                    chargeEffect.at(x, y, rotation);
                });
            }

            charging = true;

            Time.run(chargeTime, () -> {
                if(!isValid()) return;
                tr.trns(rotation, size * tilesize / 2f);
                recoil = recoilAmount;
                heat = 1f;
                bullet(type, rotation + Mathf.range(inaccuracy));
                effects();
                charging = false;
            });
        }

        @Override
        protected void updateShooting(){
            if(bulletLife > 0 && bullet != null){
                return;
            }

            if(reload <= 0 && (consValid() || cheating())){
                BulletType type = peekAmmo();

                shoot(type);

                reload = reloadTime;
            }
        }

        @Override
        protected void turnToTarget(float targetRot){
            rotation = Angles.moveToward(rotation, targetRot, efficiency() * rotateSpeed * delta() * (bulletLife > 0f ? firingMoveFract : 1f));
        }

        @Override
        protected void bullet(BulletType type, float angle){
            bullet = type.create(tile.build, team, x, y, angle);
            bulletLife = shootDuration;
        }

        @Override
        public boolean shouldActiveSound(){
            return bulletLife > 0 && bullet != null;
        }
    }
}