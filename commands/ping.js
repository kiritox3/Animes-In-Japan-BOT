
const Discord = require("discord.js");

module.exports.run = async (client, message, args) => {

  const embed = new Discord.MessageEmbed()
  .setDescription("Calculando Ping...")
  .setColor("RED")
  message.channel.send(embed).then(msg => {
    message.delete().catch(O_o => {});
    setTimeout(() => {
      let ping = new Discord.MessageEmbed()
      .setDescription(`**Bot ms:**  \`${Math.round(client.ws.ping)}ms\`\n**Server ms:**  \`${msg.createdTimestamp - message.createdTimestamp}ms\``)
      .setColor("BLUE")
      msg.edit(ping)
    
    }, 3000)
  })
}