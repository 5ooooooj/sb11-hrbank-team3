package com.hrbank3.hrbank3.mapper;

import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

public interface GenericMapper<D, E> {
  // Entity -> Dto로 변환
  D toDto(E entity);

  // Dto -> Entity로 변환
  E toEntity(D dto);

  // Entity 리스트 -> Dto 리스트로 변환
  List<D> toDtoList(List<E> entityList);

  // Dto 리스트 -> Entity 리스트로 변환
  List<E> toEntityList(List<D> dtoList);

  // null 값일 경우 무시하고 아닌 경우에만 치환
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateFromDto(D dto, @MappingTarget E entity);
}
