# Cyclotron
 Cyclotron is a plugin for PaperMC servers.
 
# Features
- Auto Crafting
- Filter (wip)
 
# Commands
for all players
- `/cyclotron <register | unregister> <recipe name>`
  - Register / unregister the specified recipe.
- `/cyclotron info`
  - Show recipes what are registered to the specified block.
- `/cyclotron limit`
  - Show the limit of recipe amount what you can register to containers.

---

for admin
- `/cyclotron clear` : (Requires permission `cyclotron.clear`)
  - Clear all data what are registered to the specified block.
- `/cyclotron reload` : (Requires permission `cyclotron.reload`)
  - Reload `config.yml`. (read, apply)
- `/cyclotron change_recipes_limit <1~99999999>` : (Requires permission `cyclotron.change_recipes_limit`)
  - Change the limit what you can register recipes to containers in range of 1 ~ 99999999.