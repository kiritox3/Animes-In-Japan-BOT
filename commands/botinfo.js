const Discord = require("discord.js");
 
exports.run = (client, message, args) => {
    const embed = new Discord.MessageEmbed()
    .setColor('BLACK')
    .setDescription(`**Botinfo:**`)
    .setTimestamp()
    .setFooter(`Author: ${message.author.username}`)
    .addFields(
        {
            name: 'Nickname:',
            value: `${client.user.tag}`,
            inline: true
        },
        {
            name: 'Servers:',
            value: `Estou em **${client.guilds.cache.size}** servidores.`,
            inline: true
        },
        {
            name: 'Channels:',
            value: `Acesso a **${client.channels.cache.size}** canais de texto.`,
            inline: true
        },
        {
            name: 'Users:',
            value: `Somente **${client.users.cache.size}** usuários.`,
            inline: true
        },
        {
            name: 'Ping:',
            value: `**${Math.round(client.ws.ping)}** ms`,
            inline: true
        },
        {
            name: 'Creator:',
            value: 'Foto de anime no perfil#7720',
            inline: true
        },
        {
            name: 'My server:',
            value: 'https://discord.gg/JjMjJrY6js ',
            inline: true
        },
    )
    message.channel.send(embed);
}