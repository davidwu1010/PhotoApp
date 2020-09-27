package com.photoapp.api.users.service;

import com.photoapp.api.users.data.AlbumsServiceClient;
import com.photoapp.api.users.data.UserEntity;
import com.photoapp.api.users.data.UsersRepository;
import com.photoapp.api.users.shared.UserDto;
import com.photoapp.api.users.ui.models.AlbumResponseModel;
import feign.FeignException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsersServiceImpl implements UsersService {

    private UsersRepository usersRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    //    private RestTemplate restTemplate;
    private final AlbumsServiceClient albumsServiceClient;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UsersServiceImpl(UsersRepository usersRepository,
        BCryptPasswordEncoder bCryptPasswordEncoder,
        AlbumsServiceClient albumsServiceClient) {
        this.usersRepository = usersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.albumsServiceClient = albumsServiceClient;
    }

    @Override
    public UserDto createUser(UserDto userDetails) {
        userDetails.setUserId(UUID.randomUUID().toString());
        userDetails.setEncryptedPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = modelMapper.map(userDetails, UserEntity.class);
        usersRepository.save(userEntity);

        UserDto returnValue = modelMapper.map(userEntity, UserDto.class);

        return returnValue;
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = usersRepository.findByEmail(email);
        if (userEntity == null) {
            throw new UsernameNotFoundException(email);
        }

        return new ModelMapper().map(userEntity, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = usersRepository.findByUserId(userId);
        if (userEntity == null) {
            throw new UsernameNotFoundException("User Not Found");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
        List<AlbumResponseModel> albumsList = null;
        try {
            albumsList = albumsServiceClient.getAlbums(userId);
        } catch (FeignException e) {
            logger.error(e.getLocalizedMessage());
        }
        userDto.setAlbums(albumsList);
        return userDto;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = usersRepository.findByEmail(username);
        if (userEntity == null) {
            throw new UsernameNotFoundException(username);
        }
        return new User(username, userEntity.getEncryptedPassword(), true, true, true, true,
            new ArrayList<>());
    }
}
