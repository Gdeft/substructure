const sCrafter = extendContent(GenericCrafter, "selective-crafter", {
  load(){
    this.region = Core.atlas.find(this.name);
    this.liquidRegion = Core.atlas.find(this.name + "-liquid");
    this.topRegion = Core.atlas.find(this.name + "-top");
  },

  draw(tile){
    entity = tile.ent();

    Draw.rect(this.region, tile.drawx(), tile.drawy());
    if(entity.liquids.total() > 0.001){
      Draw.color(entity.liquids.current().color);
      Draw.alpha(entity.liquids.total() / this.liquidCapacity);
      Draw.rect(this.liquidRegion, tile.drawx(), tile.drawy());
      Draw.color();'
    }
    Draw.rect(this.topRegion, tile.drawx(), tile.drawy());
  },

  update(tile){
    entity = tile.ent();
    this.super$update(tile);
  }
});

sCrafter.size = 3;
sCrafter.hasLiquids = true;
sCrafter.hasItems = true;
sCrafter.hasPower = true;
sCrafter.requirements = ItemStack.with(Items.copper, 60, Items.lead, 40, Items.titanium, 35);
sCrafter.consumes.item(Items.sand, 1);
sCrafter.consumes.power(1.15);
sCrafter.consumes.liquid(Liquids.water, 0.6);
sCrafter.outputItem = new ItemStack(Items.scrap, 1);
sCrafter.category = Category.crafting;
sCrafter.buildVisibility = BuildVisibility.shown;
sCrafter.convTimer = sCrafter.timers++;

sCrafter.entityType = prov(() => {
  const entity = extend(GenericCrafter.GenericCrafterEntity, {

  });
  return entity;
});
