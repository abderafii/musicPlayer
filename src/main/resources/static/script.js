const player = document.getElementById('player');
const folderList = document.getElementById('folderList');
const songList = document.getElementById('songList');
const currentSong = document.getElementById('currentSong');
const coverImage = document.getElementById('coverImage');
const prevBtn = document.getElementById('prevBtn');
const nextBtn = document.getElementById('nextBtn');
const shuffleBtn = document.getElementById('shuffleBtn');
const repeatBtn = document.getElementById('repeatBtn');
const folderEmpty = document.getElementById('folderEmpty');
const songEmpty = document.getElementById('songEmpty');
let folders = [];
let songs = [];
let selectedFolderName = '';
let selectedSongName = '';
let shuffleEnabled = false;
let repeatMode = 0; // 0 off, 1 repeat all, 2 repeat one
coverImage.onerror = () => {
    coverImage.src = '/default-cover.png';
};
fetch('/folders')
    .then(response => response.json())
    .then(data => {
        folders = data || [];
        if (folders.length === 0) {
            folderEmpty.style.display = 'block';
            return;
        }
        renderFolders();
    })
    .catch(error => {
        console.error('Failed to load folders:', error);
        folderEmpty.style.display = 'block';
        folderEmpty.textContent = 'Could not load folders.';
    });
function renderFolders() {
    folderList.innerHTML = '';
    folders.forEach(folder => {
        const li = document.createElement('li');
        li.textContent = folder;
        if (folder === selectedFolderName) {
            li.classList.add('active-folder');
        }
        li.onclick = () => {
            loadFolder(folder);
        };
        folderList.appendChild(li);
    });
}
function loadFolder(folderName) {
    fetch('/folders/' + encodeURIComponent(folderName) + '/songs')
        .then(response => response.json())
        .then(data => {
            selectedFolderName = folderName;
            songs = data || [];
            renderFolders();
            renderSongs();
        })
        .catch(error => {
            console.error('Failed to load songs:', error);
        });
}
function renderSongs() {
    songList.innerHTML = '';
    if (songs.length === 0) {
        songEmpty.style.display = 'block';
        songEmpty.textContent = 'No songs found in this folder.';
        return;
    }
    songEmpty.style.display = 'none';
    songs.forEach((song) => {
        const li = document.createElement('li');
        li.textContent = cleanName(song);
        if (song === selectedSongName) {
            li.classList.add('active-song');
        }
        li.onclick = () => {
            playSong(song);
        };
        songList.appendChild(li);
    });
}
function cleanName(songName) {
    return songName.replace(/\.mp3$/i, '');
}
function playSong(songName) {
    if (!selectedFolderName || !songName) return;
    player.src = '/songs/' + encodeURIComponent(selectedFolderName) + '/' + encodeURIComponent(songName);
    selectedSongName = songName;
    player.play();
    currentSong.textContent = cleanName(songName);
    // 🎨 set cover image
    coverImage.classList.remove('hidden');
    coverImage.src = '/covers/' + encodeURIComponent(selectedFolderName) + '/' + encodeURIComponent(songName);
    renderSongs();
}
function playPause() {
    if (!player.src && songs.length > 0) {
        playSong(0);
        return;
    }
    if (player.paused) {
        player.play();
    } else {
        player.pause();
    }
}
function getCurrentSongIndex() {
    return songs.indexOf(selectedSongName);
}
function playNext() {
    if (songs.length === 0) return;
    if (shuffleEnabled) {
        if (songs.length === 1) {
            playSong(songs[0]);
            return;
        }
        let nextSong;
        do {
            nextSong = songs[Math.floor(Math.random() * songs.length)];
        } while (nextSong === selectedSongName);
        playSong(nextSong);
        return;
    }
    const currentIndex = getCurrentSongIndex();
    if (currentIndex < songs.length - 1) {
        playSong(songs[currentIndex + 1]);
    } else if (currentIndex >= songs.length-1) {
        playSong(songs[0]);
    } else if (repeatMode === 1) {
        playSong(songs[0]);
    } 
}
function playPrevious() {
    if (songs.length === 0) return;
    if (shuffleEnabled) {
        if (songs.length === 1) {
            playSong(songs[0]);
            return;
        }
        let prevSong;
        do {
            prevSong = songs[Math.floor(Math.random() * songs.length)];
        } while (prevSong === selectedSongName);
        playSong(prevSong);
        return;
    }
    const currentIndex = getCurrentSongIndex();
    if (currentIndex > 0) {
        playSong(songs[currentIndex - 1]);
    } else if (currentIndex === 0) {
        playSong(songs[songs.length - 1]);
    }
}
function toggleShuffle() {
    shuffleEnabled = !shuffleEnabled;
    shuffleBtn.textContent = shuffleEnabled ? '🎲 Shuffle: On' : '🎲 Shuffle: Off';
    shuffleBtn.classList.toggle('active', shuffleEnabled);
}
function toggleRepeat() {
    repeatMode = (repeatMode + 1) % 3;
    if (repeatMode === 0) {
        repeatBtn.textContent = '🔁 Repeat: Off';
        repeatBtn.classList.remove('active');
        player.loop = false;
    } else if (repeatMode === 1) {
        repeatBtn.textContent = '🔁 Repeat: All';
        repeatBtn.classList.add('active');
        player.loop = false;
    } else {
        repeatBtn.textContent = '🔂 Repeat: One';
        repeatBtn.classList.add('active');
        player.loop = true;
    }
}
player.addEventListener('ended', () => {
    if (repeatMode === 2) return;
    if (shuffleEnabled) {
        playNext();
        return;
    }
    const currentIndex = getCurrentSongIndex();
    if (currentIndex < songs.length - 1) {
        playSong(songs[currentIndex + 1]);
    } else if (repeatMode === 1) {
        playSong(songs[0]);
    }
});
prevBtn.addEventListener('click', playPrevious);
nextBtn.addEventListener('click', playNext);
shuffleBtn.addEventListener('click', toggleShuffle);
repeatBtn.addEventListener('click', toggleRepeat);